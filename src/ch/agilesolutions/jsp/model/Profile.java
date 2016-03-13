package ch.agilesolutions.jsp.model;

import java.io.Serializable;

/**
 * The persistent class for the master database table.
 * 
 */
public class Profile implements Serializable {

	private int id;

	private long version;

	private String hostName;

	private String jbarName;

	private String component;

	private String environment;

	private String prefix;

	private String dnsName;

	private Integer buildNumber;

	private String name;

	private String description;

	private String userName;

	private String password;

	private String releaseTag;

	private String jdkPath;

	private String jbossPath;

	private String jiraKey;

	private boolean webSockets;

	private boolean dynatrace;

	private String dynatraceEnvironment;

	private String dynatraceArgument;

	private String status;

	private String jbarDescription;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getJbarName() {
		return jbarName;
	}

	public void setJbarName(String jbarName) {
		this.jbarName = jbarName;
	}

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getDnsName() {
		return dnsName;
	}

	public void setDnsName(String dnsName) {
		this.dnsName = dnsName;
	}

	public Integer getBuildNumber() {
		return buildNumber;
	}

	public void setBuildNumber(Integer buildNumber) {
		this.buildNumber = buildNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getReleaseTag() {
		return releaseTag;
	}

	public void setReleaseTag(String releaseTag) {
		this.releaseTag = releaseTag;
	}

	public String getJdkPath() {
		return jdkPath;
	}

	public void setJdkPath(String jdkPath) {
		this.jdkPath = jdkPath;
	}

	public String getJbossPath() {
		return jbossPath;
	}

	public void setJbossPath(String jbossPath) {
		this.jbossPath = jbossPath;
	}

	public String getJiraKey() {
		return jiraKey;
	}

	public void setJiraKey(String jiraKey) {
		this.jiraKey = jiraKey;
	}

	public boolean isWebSockets() {
		return webSockets;
	}

	public void setWebSockets(boolean webSockets) {
		this.webSockets = webSockets;
	}

	public boolean isDynatrace() {
		return dynatrace;
	}

	public void setDynatrace(boolean dynatrace) {
		this.dynatrace = dynatrace;
	}

	public String getDynatraceEnvironment() {
		return dynatraceEnvironment;
	}

	public void setDynatraceEnvironment(String dynatraceEnvironment) {
		this.dynatraceEnvironment = dynatraceEnvironment;
	}

	public String getDynatraceArgument() {
		return dynatraceArgument;
	}

	public void setDynatraceArgument(String dynatraceArgument) {
		this.dynatraceArgument = dynatraceArgument;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getJbarDescription() {
		return jbarDescription;
	}

	public void setJbarDescription(String jbarDescription) {
		this.jbarDescription = jbarDescription;
	}

}
