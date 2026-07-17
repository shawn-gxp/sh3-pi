namespace NHL.Services.DependencyService;

public interface IBusyIndicator
{
	bool IsBusy { get; set; }

	string Message { get; set; }

	void Initialize();

	void Show();

	void Hide();
}
