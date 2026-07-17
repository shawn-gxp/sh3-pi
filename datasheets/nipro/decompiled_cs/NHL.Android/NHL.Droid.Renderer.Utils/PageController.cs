using System.Collections.ObjectModel;
using Xamarin.Forms;

namespace NHL.Droid.Renderer.Utils;

public class PageController : IPageController
{
	private ReflectedProxy<Page> _proxy;

	public Rectangle ContainerArea
	{
		get
		{
			return _proxy.GetPropertyValue<Rectangle>("ContainerArea");
		}
		set
		{
			_proxy.SetPropertyValue(value, "ContainerArea");
		}
	}

	public bool IgnoresContainerArea
	{
		get
		{
			return _proxy.GetPropertyValue<bool>("IgnoresContainerArea");
		}
		set
		{
			_proxy.SetPropertyValue(value, "IgnoresContainerArea");
		}
	}

	public ObservableCollection<Element> InternalChildren
	{
		get
		{
			return _proxy.GetPropertyValue<ObservableCollection<Element>>("InternalChildren");
		}
		set
		{
			_proxy.SetPropertyValue(value, "InternalChildren");
		}
	}

	public static IPageController Create(Page page)
	{
		return new PageController(page);
	}

	private PageController(Page page)
	{
		_proxy = new ReflectedProxy<Page>(page);
	}

	public void SendAppearing()
	{
		_proxy.Call("SendAppearing");
	}

	public void SendDisappearing()
	{
		_proxy.Call("SendDisappearing");
	}
}
