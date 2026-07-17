using System;
using System.Reflection;
using NHL.Services.DependencyService;
using Xamarin.Forms;

namespace NHL.Views.Controls;

public class NHLToolbarItem : ToolbarItem
{
	private ILoggingService Log = DependencyService.Get<ILoggingService>();

	public static readonly BindableProperty MethodNameProperty = BindableProperty.Create("MethodName", typeof(string), typeof(NHLToolbarItem));

	public string MethodName
	{
		get
		{
			return (string)GetValue(MethodNameProperty);
		}
		set
		{
			SetValue(MethodNameProperty, value);
		}
	}

	protected override void OnClicked()
	{
		base.OnClicked();
		if (string.IsNullOrEmpty(MethodName))
		{
			return;
		}
		try
		{
			MethodInfo methodInfo = base.BindingContext?.GetType().GetTypeInfo().GetDeclaredMethod(MethodName);
			if ((object)methodInfo != null)
			{
				methodInfo.Invoke(base.BindingContext, null);
			}
			else
			{
				(base.BindingContext?.GetType().GetTypeInfo().BaseType.GetTypeInfo().GetDeclaredMethod(MethodName))?.Invoke(base.BindingContext, null);
			}
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【NHLToolbarItem】【OnClicked】例外発生：{ex}");
		}
	}
}
