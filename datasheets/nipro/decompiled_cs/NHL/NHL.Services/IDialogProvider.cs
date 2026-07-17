using System;
using System.Threading.Tasks;
using Xamarin.Forms;

namespace NHL.Services;

public interface IDialogProvider
{
	void Initialize(Page dialogPage);

	Task ShowAlert(string title, string message);

	Task ShowAlert(string title, string message, string buttonText, Action afterHideCallback);

	Task<bool> ShowAlert(string title, string message, string buttonConfirmText, string buttonCancelText, Action<bool> afterHideCallback);

	Task ShowError(string title, Exception error, string buttonText, Action afterHideCallback);

	Task ShowError(string title, string message, string buttonText, Action afterHideCallback);
}
