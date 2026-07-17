using System;

namespace NHL.Services.DependencyService;

public interface ISnackBar
{
	void Show(string text, int duration, string actionText, Action action);
}
