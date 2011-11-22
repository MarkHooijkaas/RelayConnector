/**
Copyright 2008, 2009 Mark Hooijkaas

This file is part of the RelayConnector framework.

The RelayConnector framework is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The RelayConnector framework is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the RelayConnector framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kisst.cordys.as400.conn;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.SocketException;

import org.kisst.cordys.as400.As400PoolSettings;
import org.kisst.cordys.util.Destroyable;
import org.kisst.props4j.Props;

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
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class As400Connection implements Destroyable {
	private static final CordysLogger logger = CordysLogger.getCordysLogger(As400Connection.class);
	private static int counter=0; // needed to create a unique name

	private final String name;
	private final As400PoolSettings settings;
	private final Props props;
	private final AS400 as400;
	public final long creationTime = new java.util.Date().getTime();

	private String executingProgram=null;
	private Job myJob;
	private String jobId;

	public boolean isExecuting() { return executingProgram!=null; }

	public As400Connection(As400PoolSettings settings, Props props) {
		this.settings=settings;
		this.props=props;
		this.name="As400Connection-"+(counter++);
		String system = settings.host.get(props);
		String user= settings.username.get(props);
		String password = settings.password.get(props);
		logger.log(Severity.DEBUG, "starting As400Connection" + system + ":" + user);
		if (settings.simulationFlag.get(props))
			as400=null; // null signals that simulation is needed 
		else if ((password == null) || password.trim().equals(""))
			as400 =  new AS400(system, user);
		else
			as400= new AS400(system, user, password);
	}

	public String getJobId() { return jobId; }

	// TODO: The Job object would normally not change, but it might if the AS400 object
	// automatically reconnects after a connection problem. I don't know if this ever happens. I doubt it
	private synchronized void rememberJob(CommandCall call) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException {
		if (myJob==null) {
			myJob = call.getServerJob();
			jobId=myJob.getNumber();
		}
	}
	private synchronized void rememberJob(ProgramCall call) throws AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException {
		if (myJob==null) {
			myJob = call.getServerJob();
			jobId=myJob.getNumber();
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

	private String getJobLogId(ProgramCall call) {
		try {
			rememberJob(call);
			JobLog jobLog = call.getServerJob().getJobLog();
			return jobLog.getUser()+":"+jobLog.getName()+":"+jobLog.getNumber();
		} 
		catch (AS400SecurityException e) { throw new RuntimeException(e); }
		catch (ErrorCompletingRequestException e) { throw new RuntimeException(e); }
		catch (IOException e) { throw new RuntimeException(e); }
		catch (InterruptedException e) { throw new RuntimeException(e); }
	}

	// helper function to cast all the Checked exceptions to unchecked exceptions
	public void execute(ProgramCall call) {
		String programName=call.getProgram();
		if (as400==null) {
			if (logger.isInfoEnabled()) {
				String msg=programName;
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

		boolean done=false;
		Monitor mon1 = null;
		Monitor mon2 = null;
		try {
			rememberJob(call);
			executingProgram=call.getProgram();
			call.setSystem(as400);
			
			mon1 = MonitorFactory.start("As400ProgramCall:"+programName);
			mon2 = MonitorFactory.start("AllAs400ProgramCalls");
			done=call.run();
			if (done) 
				return; // call went ok, return normally, otherwise an exception is thrown (see below)
		}
		catch (AS400SecurityException e) { throw new RuntimeException( getJobLogId(call)+" "+e.getMessage(), e); }
		catch (ErrorCompletingRequestException e) { throw new RuntimeException(e); }
		catch (IOException e) { throw new RuntimeException(e); }
		catch (InterruptedException e) { throw new RuntimeException(e); }
		catch (ObjectDoesNotExistException e) { throw new RuntimeException( getJobLogId(call)+" "+e.getMessage(), e); }
		catch (PropertyVetoException e) { throw new RuntimeException(e); }
		finally {
			executingProgram=null;
			if (mon1!=null) mon1.stop();
			if (mon2!=null) mon2.stop();
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
		if (myJob==null)
			return false;

		// Get a brand new connection, because the original connection is probably hanging.
		// Do not even use the ConnectionPool, because all connections might hang.
		// Performance is not that important, since this event should be very rare.
		As400Connection conn=new As400Connection(settings, props);     	
		try {
			myJob.setSystem(conn.as400);
			String status = myJob.getStatus();

			String logging="ENDING JOB ["+jobId+"] with status ["+status+"]";
			logging+=" while executing ["+executingProgram+"]\nLast loglines:\n";

			int nrofMessages=settings.nrOfMessagesToLog.get(props);
			logging += getLogLines(nrofMessages);
			logger.log(Severity.ERROR, logging);
			myJob.end(0);
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


	private String getLogLines(int nrofMessages) {
		if (myJob==null)
			return null;
		try {
			JobLog jobLog = myJob.getJobLog();
			StringBuilder logging=new StringBuilder();
			//JobLog jobLog = as400.getJobLog();
			int  l=jobLog.getLength();
			if (l<nrofMessages)
				nrofMessages=l;
			QueuedMessage[] msgs = jobLog.getMessages(l-nrofMessages, nrofMessages);
			for (int i=0; i<msgs.length; i++){
				QueuedMessage msg = msgs[i];
				msg.load();
				if ("W".equals(msg.getReplyStatus()))
					logging.append("MSGW; ");
				logging.append("Type:"+msg.getType()+"; ");
				if (msg.getSeverity()>0)
					logging.append("Severity:"+msg.getSeverity()+"; ");

				logging.append(msg.toString());
				String help=msg.getHelp();
				if (help!=null && help.length()>0)
					logging.append("; Help:"+help+"; ");
				logging.append("\n");
			}
			return logging.toString();
		}
		catch (AS400SecurityException e) { throw new RuntimeException(e); }
		catch (ErrorCompletingRequestException e) { throw new RuntimeException(e); }
		catch (InterruptedException e) { throw new RuntimeException(e); }
		catch (IOException e) { throw new RuntimeException(e); }
		catch (ObjectDoesNotExistException e) { throw new RuntimeException(e); } 
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
			rememberJob(call);
			executingProgram=command;
			int nrofTries=2; // TODO: make configurable
			Monitor mon1=null;
			Monitor mon2=null;
			while (nrofTries>0) {
				try {
					mon1 = MonitorFactory.start("As400CommandCall:"+command);
					mon2 = MonitorFactory.start("AllAs400CommandCalls");
					ok=call.run(command);
					nrofTries=0;
				} catch (SocketException e) {
					nrofTries--;
					if (nrofTries>0)
						logger.log(Severity.WARN, "retrying after SocketException "+e.toString());
					else
						throw new RuntimeException("CommandCall failed: " + command+": "+e.toString(),e);
				}
				finally {
					if (mon1!=null) mon1.stop();
					if (mon2!=null) mon2.stop();
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