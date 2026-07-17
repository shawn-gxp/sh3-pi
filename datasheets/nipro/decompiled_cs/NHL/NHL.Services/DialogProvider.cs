using System;
using System.Threading.Tasks;
using Xamarin.Forms;

namespace NHL.Services;

public class DialogProvider : IDialogProvider
{
	private Page _dialogPage;

	public void Initialize(Page dialogPage)
	{
		_dialogPage = dialogPage;
	}

	public async Task ShowError(string title, string message, string buttonText, Action afterHideCallback)
	{
		await _dialogPage.DisplayAlert(title, message, buttonText);
		afterHideCallback?.Invoke();
	}

	public async Task ShowError(string title, Exception error, string buttonText, Action afterHideCallback)
	{
		await _dialogPage.DisplayAlert(title, error.Message, buttonText);
		afterHideCallback?.Invoke();
	}

	public async Task ShowAlert(string title, string message)
	{
		await _dialogPage.DisplayAlert(title, message, "OK");
	}

	public async Task ShowAlert(string title, string message, string buttonText, Action afterHideCallback)
	{
		await _dialogPage.DisplayAlert(title, message, buttonText);
		afterHideCallback?.Invoke();
	}

	public async Task<bool> ShowAlert(string title, string message, string buttonConfirmText, string buttonCancelText, Action<bool> afterHideCallback)
	{
		bool flag = await _dialogPage.DisplayAlert(title, message, buttonConfirmText, buttonCancelText);
		afterHideCallback?.Invoke(flag);
		return flag;
	}
}
