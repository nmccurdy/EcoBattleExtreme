package starjava;

import java.io.PrintStream;

public interface AgentMonitorObserver {
	public void setAgentMonitored(int who, boolean monitored);
	public boolean outputStatusInfo(int who, PrintStream os);
}
