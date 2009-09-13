package org.kisst.cordys.as400.conn;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.SocketException;

import org.kisst.cordys.util.Destroyable;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.AS400Text;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.Job;
import com.ibm.as400.access.JobLog;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.ProgramCall;
import com.ibm.as400.access.ProgramParameter;
import com.ibm.as400.access.QueuedMessage;
import com.ibm.as400.access.SocketProperties;

public class As400Connection implements Destroyable {
	private static final CordysLogger logger = CordysLogger.getCordysLogger(As400Connection.class);
	private static int counter=0; // needed to create a unique name
	
	private final String name;
	private final PoolConfiguration conf;
	private final AS400 as400;
    public final long creationTime = new java.util.Date().getTime();
	
    private String executingProgram=null;
    private Job activeJob;
	private String jobId;
    
    public boolean isExecuting() { return executingProgram!=null; }

	public As400Connection(PoolConfiguration conf, AS400 as400) {
		this.name="As400Connection-"+(counter++);
		this.conf=conf;
		this.as400=as400;
		setTimeout();
	}
	
	public As400Connection(PoolConfiguration conf) {
		this.name="As400Connection-"+(counter++);
		this.conf=conf;
		logger.log(Severity.DEBUG, "starting As400Connection" + conf.getHost() + ":" + conf.getUser());
		String system = conf.getHost();
		String user= conf.getUser();
		String password = conf.getPassword();
		if (conf.getSimulationFlag())
			as400=null; // null signals that simulation is needed 
		else if ((password == null) || password.trim().equals(""))
			as400 =  new AS400(system, user);
		else
			as400= new AS400(system, user, password);
		setTimeout();
	}

	private void setTimeout() {
		if (as400!=null && conf.getSocketTimeout()!=0) {
			SocketProperties props = as400.getSocketProperties();
			props.setSoTimeout(conf.getSocketTimeout());
			as400.setSocketProperties(props);
		}
	}

	public void execute(String programPath, ProgramParameter[] as400par) {
    	ProgramCall call= new ProgramCall();
    	try {
    		call.setProgram(programPath, as400par);
    	}
		catch (PropertyVetoException e) { throw new RuntimeException(e); }
		execute(call);
    }
    	
	
    // helper function to cast all the Checked exceptions to unchecked exceptions
    public void execute(ProgramCall call) {
    	if (as400==null) {
    		if (logger.isInfoEnabled()) {
    			String msg=call.getProgram();
    			ProgramParameter[] params = call.getParameterList();
    			String sep="(";
    			for (int i=0; i<params.length; i++) {
    				byte[] bytes=params[i].getInputData();
    				// TODO: be able to work with structs etc.. this may not be best place tot do this
    				AS400Text conv = new AS400Text(bytes.length);
    				msg+=sep+conv.toObject(bytes);
    				sep=",";
    			}
    			msg+=")";
   				logger.log(Severity.INFO,msg);
    		}
    		// do nothing because of simulation mode
    		return; 
    	}

	    try {
    		executingProgram=call.getProgram();
	    	call.setSystem(as400);
	    	activeJob = call.getServerJob();
	    	jobId = activeJob.getNumber();
    		
			if (call.run()) 
				return; // call went ok, return normally, otherwise an exception is thrown (see below)
		}
	    catch (AS400SecurityException e) { throw new RuntimeException(e); }
		catch (ErrorCompletingRequestException e) { throw new RuntimeException(e); }
		catch (IOException e) { throw new RuntimeException(e); }
		catch (InterruptedException e) { throw new RuntimeException(e); }
		catch (ObjectDoesNotExistException e) { throw new RuntimeException(e); }
		catch (PropertyVetoException e) { throw new RuntimeException(e); }
		finally {
			activeJob=null;
			executingProgram=null;
		}

		// Something went wrong, throw an exception with the messagelist
		StringBuilder sb= new StringBuilder("ProgramCall.run() returned false\n");
		AS400Message[] messagelist = call.getMessageList();
		for (int i = 0; i < messagelist.length; ++i) {
			sb.append(messagelist[i].getText());
			sb.append("\n");
		}
		throw new RuntimeException(sb.toString());
    }		

    /** 
     * Tries to kill the current job using ENDJOB
     * @return true if a MessageWaiting was indeed cancelled, false otherwise
     */
    public boolean killJob() {
    	// TODO: It should also be possible to kill the job when there is not
    	// an program or command running, but we can only get a Job object from a call object.
    	// This Job object would normally not change, but it might if the AS400 object
    	// automatically reconnects after a connection problem.
    	if (activeJob==null)
    		return false;
    	
    	// Get a brand new connection, because the original connection is probably hanging.
    	// Do not even use the ConnectionPool, because all connections might hang.
    	// Performance is not that important, since this event should be very rare.
    	As400Connection conn=new As400Connection(this.conf);     	
		try {
	    	activeJob.setSystem(conn.as400);
	    	JobLog jobLog = activeJob.getJobLog();
			String status = activeJob.getStatus();

	    	String logging="ENDING JOB ["+jobId+"] with status ["+status+"]";
	    	logging+=" while executing ["+executingProgram+"]\nLast loglines:\n";
	    	
			int nrofMessages=conf.getNrOfMessagesToLog();
			int  l=jobLog.getLength();
			if (l<nrofMessages)
				nrofMessages=l;
	    	QueuedMessage[] msgs = jobLog.getMessages(l-nrofMessages, nrofMessages);
	    	for (int i=0; i<msgs.length; i++){
	    		QueuedMessage msg = msgs[i];
	    		msg.load();
	    		if ("W".equals(msg.getReplyStatus()))
	    			logging+="MSGW; ";
	    		logging+="Type:"+msg.getType()+"; ";
	    		if (msg.getSeverity()>0)
	    			logging+="Severity:"+msg.getSeverity()+"; ";

				logging+=msg.toString();
	    		String help=msg.getHelp();
	    		if (help!=null && help.length()>0)
	    			logging+="; Help:"+help+"; ";
	    		logging+="\n";
	    	}
	    	logger.log(Severity.ERROR, logging);
			activeJob.end(0);
			return true;
		}
		catch (AS400SecurityException e) { throw new RuntimeException(e); }
		catch (ErrorCompletingRequestException e) { throw new RuntimeException(e); }
		catch (InterruptedException e) { throw new RuntimeException(e); }
		catch (IOException e) { throw new RuntimeException(e); }
		catch (ObjectDoesNotExistException e) { throw new RuntimeException(e); } 
		catch (PropertyVetoException e) { throw new RuntimeException(e); }
		finally {
			conn.close();
		}
    }
    
	public void executeCommand(String command)
	{
    	if (as400==null) {
			logger.log(Severity.INFO,command);
    		return; // do nothing because of simulation mode
    	}
		
		CommandCall call = new CommandCall(as400);
		
		boolean ok=true; // needs to be set here, otherwise compiler warns that it might not be set
		try {
			executingProgram=command;
	    	activeJob = call.getServerJob();
	    	jobId = activeJob.getNumber();
			int nrofTries=2; // TODO: make configurable
			while (nrofTries>0) {
				try {
					ok=call.run(command);
					nrofTries=0;
				} catch (SocketException e) {
					nrofTries--;
					if (nrofTries>0)
						logger.log(Severity.WARN, "retrying after SocketException "+e.toString());
					else
						throw new RuntimeException("CommandCall failed: " + command+": "+e.toString(),e);
				}
			}
		} catch (AS400SecurityException e) {
			throw new RuntimeException("CommandCall failed: " + command+": "+e.toString(),e);
		} catch (ErrorCompletingRequestException e) {
			throw new RuntimeException("CommandCall failed: " + command+": "+e.toString(),e);
		} catch (IOException e) {
			throw new RuntimeException("CommandCall failed: " + command+": "+e.toString(),e);
		} catch (InterruptedException e) {
			throw new RuntimeException("CommandCall failed: " + command+": "+e.toString(),e);
		} catch (PropertyVetoException e) {
			throw new RuntimeException("CommandCall failed: " + command+": "+e.toString(),e);
		}
		finally {
			activeJob=null;
			executingProgram=null;
		}
		if (!ok) {
			String msg = "CommandCall failed: " + command+"\n";
			AS400Message[] lst = call.getMessageList();
			for (int i=0; i<lst.length; i++ ) {
				msg += lst[i].toString()+"\n";
			}
			throw new RuntimeException(msg);	
		}
	}
	
	public void close() {
    	if (as400==null)
    		return; // do nothing because of simulation mode
		as400.disconnectAllServices();		
	}

	public int getCcsid() {
    	if (as400==null)
    		return 1140; // do nothing because of simulation mode
		return as400.getCcsid();
	}

	public String getName() {
		return name;
	}

	public String getRunningProgam() {
		return executingProgram;
	}

	public void destroy() {
		close();
	}
}