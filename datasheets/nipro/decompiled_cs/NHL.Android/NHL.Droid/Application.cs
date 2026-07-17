using System;
using System.Collections.Generic;
using System.Reflection;
using Android.Runtime;
using Caliburn.Micro;

namespace NHL.Droid;

public class Application : CaliburnApplication
{
	private SimpleContainer container;

	public Application(IntPtr javaReference, JniHandleOwnership transfer)
		: base(javaReference, transfer)
	{
	}

	public override void OnCreate()
	{
		base.OnCreate();
		AppDomain.CurrentDomain.UnhandledException += delegate
		{
		};
		Initialize();
	}

	protected override void Configure()
	{
		container = new SimpleContainer();
		container.Instance(container);
		container.Singleton<App>();
	}

	protected override IEnumerable<Assembly> SelectAssemblies()
	{
		return new Assembly[2]
		{
			GetType().Assembly,
			typeof(App).Assembly
		};
	}

	protected override void BuildUp(object instance)
	{
		container.BuildUp(instance);
	}

	protected override IEnumerable<object> GetAllInstances(Type service)
	{
		return container.GetAllInstances(service);
	}

	protected override object GetInstance(Type service, string key)
	{
		object instance = container.GetInstance(service, key);
		IoC.BuildUp(instance);
		return instance;
	}
}
