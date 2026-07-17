namespace NHL.Services.DependencyService;

public interface IApplicationSleepControlService
{
	void SleepDisabled(bool disabled = true);
}
