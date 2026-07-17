using System;
using System.Reflection;
using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.Views.Behaviors.Support;

public abstract class CallMethodBehaviorBase<T> : Behavior<T> where T : View
{
	public static readonly BindableProperty MethodNameProperty = BindableProperty.Create("MethodName", typeof(string), typeof(string));

	public static readonly BindableProperty EventNameProperty = BindableProperty.Create("EventName", typeof(string), typeof(string));

	public static readonly BindableProperty TargetObjectProperty = BindableProperty.Create("TargetObject", typeof(object), typeof(object));

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

	public string EventName
	{
		get
		{
			return (string)GetValue(EventNameProperty);
		}
		set
		{
			SetValue(EventNameProperty, value);
		}
	}

	public object TargetObject
	{
		get
		{
			return GetValue(TargetObjectProperty);
		}
		set
		{
			SetValue(TargetObjectProperty, value);
		}
	}

	protected abstract void AddGesture(T associatedObject);

	protected abstract void RemoveGesture(T associatedObject);

	protected override void OnAttachedTo(T bindable)
	{
		base.OnAttachedTo(bindable);
		bindable.BindingContextChanged += OnViewBindingContextChanged;
		base.BindingContext = bindable.BindingContext;
		AddGesture(bindable);
	}

	protected override void OnDetachingFrom(T bindable)
	{
		RemoveGesture(bindable);
		bindable.BindingContextChanged -= OnViewBindingContextChanged;
		base.BindingContext = null;
		base.OnDetachingFrom(bindable);
	}

	protected virtual void CallMethod()
	{
		if (TargetObject != null && !string.IsNullOrEmpty(MethodName))
		{
			MethodInfo declaredMethod = TargetObject.GetType().GetTypeInfo().GetDeclaredMethod(MethodName);
			if ((object)declaredMethod != null)
			{
				declaredMethod.Invoke(TargetObject, null);
			}
			else if (TargetObject is ViewModelBase)
			{
				TargetObject.GetType().GetTypeInfo().BaseType.GetTypeInfo().GetDeclaredMethod(MethodName)?.Invoke(TargetObject, null);
			}
		}
	}

	protected void OnViewBindingContextChanged(object sender, EventArgs e)
	{
		base.BindingContext = ((T)sender).BindingContext;
	}
}
