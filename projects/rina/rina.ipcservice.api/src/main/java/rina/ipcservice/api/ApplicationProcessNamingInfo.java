package rina.ipcservice.api;

import org.apache.commons.validator.UrlValidator;

/**
 * All the elements needed to name an application process.
 *
 */
public class ApplicationProcessNamingInfo {
	
	private String applicationProcessName = null;
	private String applicationProcessInstance = null;
	private String applicationEntityName = null;
	private String applicationEntityInstance = null;
	
	
	public ApplicationProcessNamingInfo(String applicationProcessName, String applicationProcessInstance, String applicationEntityName, String applicationEntityInstance){
		this.applicationProcessName = applicationProcessName;
		this.applicationProcessInstance = applicationProcessInstance;
		this.applicationEntityName = applicationEntityName;
		this.applicationEntityInstance = applicationEntityInstance;
	}
	
	public String getApplicationProcessName() {
		return applicationProcessName;
	}

	public void setApplicationProcessName(String applicationProcessName) {
		this.applicationProcessName = applicationProcessName;
	}

	public String getApplicationProcessInstance() {
		return applicationProcessInstance;
	}

	public void setApplicationProcessInstance(String applicationProcessInstance) {
		this.applicationProcessInstance = applicationProcessInstance;
	}

	public String getApplicationEntityName() {
		return applicationEntityName;
	}

	public void setApplicationEntityName(String applicationEntityName) {
		this.applicationEntityName = applicationEntityName;
	}

	public String getApplicationEntityInstance() {
		return applicationEntityInstance;
	}

	public void setApplicationEntityInstance(String applicationEntityInstance) {
		this.applicationEntityInstance = applicationEntityInstance;
	}

	public static void validateApplicationProcessNamingInfo(ApplicationProcessNamingInfo APnamingInfo) throws Exception{
		validateApplicationProcessName(APnamingInfo.applicationProcessName);
		validateApplicationProcessInstance(APnamingInfo.applicationProcessInstance);
		validateApplicationEntityName(APnamingInfo.applicationEntityName);
		validateApplicationEntityInstance(APnamingInfo.applicationEntityInstance);
	}
	
	private static void validateApplicationProcessName(String applicationProcessName) throws Exception
	{
		if (applicationProcessName!=null)
		{
			UrlValidator urlValidator = new UrlValidator();
			if (!urlValidator.isValid(applicationProcessName))
				throw new Exception("Application process name is not a valid URL");
		}
		else
			throw new Exception("Application process name is empty");
	}
	
	
	private static void validateApplicationProcessInstance(String applicationProcessInstance) throws Exception
	{
		if (applicationProcessInstance!=null)
		{
			//TODO: has to be unique within the AP
			try
			{
				Integer.parseInt(applicationProcessInstance);
			}
			catch(NumberFormatException nfe)
			{
				System.out.println("Application process instance is not an interger");
			}
		}
		else
			throw new Exception("Application process instance is empty");	
	}
	
	
	
	private static void validateApplicationEntityName(String ApplicationEntityName) throws Exception
	{
		if (ApplicationEntityName!=null)
		{
			//TODO: add format check
		}
		else
			throw new Exception("Application entity name is empty");
	}
	
	
	
	private static void validateApplicationEntityInstance(String applicationEntityInstance) throws Exception
	{
		if (applicationEntityInstance!=null)
		{
			try
			{
				Integer.parseInt(applicationEntityInstance);
			}
			catch(NumberFormatException nfe)
			{
				System.out.println("Application entity instance is not an interger");
			}
		}
		else
			throw new Exception("Application entity instance is empty");
	}

		
	
}