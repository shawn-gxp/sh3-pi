namespace NHL.Services.DependencyService;

public interface IRequestLocation
{
	bool IsLocationServiceEnabled();

	void Request();
}
