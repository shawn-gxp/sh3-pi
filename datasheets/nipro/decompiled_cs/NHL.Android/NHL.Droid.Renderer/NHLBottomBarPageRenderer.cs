using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using Android.Content;
using Android.Views;
using Android.Widget;
using BottomNavigationBar;
using BottomNavigationBar.Listeners;
using NHL.Droid.Renderer.Utils;
using NHL.Views.Renderer;
using Xamarin.Forms;
using Xamarin.Forms.Platform.Android;
using Xamarin.Forms.Platform.Android.AppCompat;

namespace NHL.Droid.Renderer;

public class NHLBottomBarPageRenderer : VisualElementRenderer<BottomBarPage>, IOnTabClickListener
{
	private bool _disposed;

	private BottomBar _bottomBar;

	private FrameLayout _frameLayout;

	private NHL.Droid.Renderer.Utils.IPageController _pageController;

	private IDictionary<Page, BottomBarBadge> _badges;

	private static int maxHeight;

	private int childHeight;

	public NHLBottomBarPageRenderer()
	{
		base.AutoPackage = false;
	}

	public virtual void OnTabSelected(int position)
	{
		SwitchContent(base.Element.Children[position]);
		base.Element.CurrentPage = base.Element.Children[position];
	}

	public virtual void OnTabReSelected(int position)
	{
	}

	protected override void Dispose(bool disposing)
	{
		if (disposing && !_disposed)
		{
			_disposed = true;
			((ViewGroup)(object)this).RemoveAllViews();
			foreach (Page child in base.Element.Children)
			{
				IVisualElementRenderer renderer = Platform.GetRenderer(child);
				if (renderer != null)
				{
					renderer.ViewGroup.RemoveFromParent();
					renderer.Dispose();
				}
				child.PropertyChanged -= OnPagePropertyChanged;
			}
			if (_badges != null)
			{
				_badges.Clear();
				_badges = null;
			}
			if (_bottomBar != null)
			{
				_bottomBar.SetOnTabClickListener(null);
				_bottomBar.Dispose();
				_bottomBar = null;
			}
			if (_frameLayout != null)
			{
				_frameLayout.Dispose();
				_frameLayout = null;
			}
		}
		base.Dispose(disposing);
	}

	protected override void OnAttachedToWindow()
	{
		((Android.Views.View)(object)this).OnAttachedToWindow();
		_pageController.SendAppearing();
	}

	protected override void OnDetachedFromWindow()
	{
		((Android.Views.View)(object)this).OnDetachedFromWindow();
		_pageController.SendDisappearing();
	}

	protected override void OnElementChanged(ElementChangedEventArgs<BottomBarPage> e)
	{
		base.OnElementChanged(e);
		if (e.NewElement == null)
		{
			return;
		}
		BottomBarPage newElement = e.NewElement;
		if (_bottomBar == null)
		{
			_pageController = PageController.Create(newElement);
			_frameLayout = new FrameLayout(Forms.Context);
			_frameLayout.LayoutParameters = new FrameLayout.LayoutParams(-1, -1, GravityFlags.Fill);
			((ViewGroup)(object)this).AddView((Android.Views.View)_frameLayout, 0);
			_bottomBar = BottomBar.Attach(_frameLayout, null);
			_bottomBar.NoTabletGoodness();
			if (newElement.FixedMode)
			{
				_bottomBar.UseFixedMode();
			}
			switch (newElement.BarTheme)
			{
			case BottomBarPage.BarThemeTypes.DarkWithAlpha:
				_bottomBar.UseDarkThemeWithAlpha();
				break;
			case BottomBarPage.BarThemeTypes.DarkWithoutAlpha:
				_bottomBar.UseDarkThemeWithAlpha(useDarkThemeAlpha: false);
				break;
			default:
				throw new ArgumentOutOfRangeException();
			case BottomBarPage.BarThemeTypes.Light:
				break;
			}
			_bottomBar.LayoutParameters = new ViewGroup.LayoutParams(-1, -1);
			_bottomBar.SetOnTabClickListener(this);
			UpdateTabs();
			UpdateBarBackgroundColor();
			UpdateBarTextColor();
		}
		if (newElement.CurrentPage != null)
		{
			SwitchContent(newElement.CurrentPage);
		}
	}

	protected override void OnElementPropertyChanged(object sender, PropertyChangedEventArgs e)
	{
		base.OnElementPropertyChanged(sender, e);
		if (e.PropertyName == "CurrentPage")
		{
			SwitchContent(base.Element.CurrentPage);
			UpdateSelectedTabIndex(base.Element.CurrentPage);
		}
		else if (e.PropertyName == NavigationPage.BarBackgroundColorProperty.PropertyName)
		{
			UpdateBarBackgroundColor();
		}
		else if (e.PropertyName == NavigationPage.BarTextColorProperty.PropertyName)
		{
			UpdateBarTextColor();
		}
	}

	protected virtual void SwitchContent(Page view)
	{
		((Android.Views.View)(object)this).Context.HideKeyboard((Android.Views.View)(object)this);
		_frameLayout.RemoveAllViews();
		if (view != null)
		{
			if (Platform.GetRenderer(view) == null)
			{
				Platform.SetRenderer(view, Platform.CreateRenderer(view));
			}
			_frameLayout.AddView(Platform.GetRenderer(view).ViewGroup);
		}
	}

	protected override void OnLayout(bool changed, int l, int t, int r, int b)
	{
		if (changed)
		{
			if (b > maxHeight)
			{
				return;
			}
			int num = r - l;
			if (childHeight > 0)
			{
				_pageController.ContainerArea = new Rectangle(0.0, 0.0, ((Android.Views.View)(object)this).Context.FromPixels(num), ((Android.Views.View)(object)this).Context.FromPixels(childHeight));
			}
			else
			{
				_pageController.ContainerArea = new Rectangle(0.0, 0.0, ((Android.Views.View)(object)this).Context.FromPixels(num), ((Android.Views.View)(object)this).Context.FromPixels(_frameLayout.MeasuredHeight));
			}
			ObservableCollection<Element> internalChildren = _pageController.InternalChildren;
			for (int i = 0; i < internalChildren.Count; i++)
			{
				if (internalChildren[i] is VisualElement bindable)
				{
					IVisualElementRenderer renderer = Platform.GetRenderer(bindable);
					if (renderer is NavigationPageRenderer)
					{
						renderer.UpdateLayout();
					}
				}
			}
			_bottomBar.Measure(MeasureSpecFactory.MakeMeasureSpec(num, MeasureSpecMode.Exactly), MeasureSpecFactory.MakeMeasureSpec(maxHeight, MeasureSpecMode.Exactly));
			_bottomBar.Layout(0, 0, num, maxHeight);
			base.OnLayout(changed, l, t, r, maxHeight);
			return;
		}
		if (maxHeight == 0)
		{
			maxHeight = b;
		}
		if (childHeight == 0 || childHeight < _frameLayout.MeasuredHeight)
		{
			childHeight = _frameLayout.MeasuredHeight;
		}
		int num2 = r - l;
		int num3 = b - t;
		Context context = ((Android.Views.View)(object)this).Context;
		_bottomBar.Measure(MeasureSpecFactory.MakeMeasureSpec(num2, MeasureSpecMode.Exactly), MeasureSpecFactory.MakeMeasureSpec(num3, MeasureSpecMode.AtMost));
		int num4 = Math.Min(num3, Math.Max(_bottomBar.MeasuredHeight, _bottomBar.MinimumHeight));
		if (num2 > 0 && num3 > 0)
		{
			_pageController.ContainerArea = new Rectangle(0.0, 0.0, context.FromPixels(num2), context.FromPixels(_frameLayout.MeasuredHeight));
			ObservableCollection<Element> internalChildren2 = _pageController.InternalChildren;
			for (int j = 0; j < internalChildren2.Count; j++)
			{
				if (internalChildren2[j] is VisualElement bindable2)
				{
					_ = Platform.GetRenderer(bindable2) is NavigationPageRenderer;
				}
			}
			_bottomBar.Measure(MeasureSpecFactory.MakeMeasureSpec(num2, MeasureSpecMode.Exactly), MeasureSpecFactory.MakeMeasureSpec(num4, MeasureSpecMode.Exactly));
			_bottomBar.Layout(0, 0, num2, num4);
		}
		base.OnLayout(changed, l, t, r, b);
	}

	private void UpdateSelectedTabIndex(Page page)
	{
		int position = base.Element.Children.IndexOf(page);
		_bottomBar.SelectTabAtPosition(position, animate: true);
	}

	private void UpdateBarBackgroundColor()
	{
		if (!_disposed && _bottomBar != null)
		{
			_bottomBar.SetBackgroundColor(base.Element.BarBackgroundColor.ToAndroid());
		}
	}

	private void UpdateBarTextColor()
	{
		if (!_disposed && _bottomBar != null)
		{
			_bottomBar.SetActiveTabColor(base.Element.BarTextColor.ToAndroid());
		}
	}

	private void UpdateTabs()
	{
		SetTabItems();
		SetTabColors();
		SetTabBadges();
		AddPropertyChangedHandlersForPages();
	}

	private void SetTabItems()
	{
		BottomBarTab[] array = base.Element.Children.Select((Page page) => new BottomBarTab(ResourceManagerEx.IdFromTitle(page.Icon, ResourceManager.DrawableClass), page.Title)).ToArray();
		if (array.Length != 0)
		{
			_bottomBar.SetItems(array);
		}
	}

	private void SetTabColors()
	{
		for (int i = 0; i < base.Element.Children.Count; i++)
		{
			Color tabColor = BottomBarPageExtensions.GetTabColor(base.Element.Children[i]);
			if (tabColor != Color.Transparent)
			{
				_bottomBar.MapColorForTab(i, tabColor.ToAndroid());
			}
		}
	}

	private void SetTabBadges()
	{
		_badges = new Dictionary<Page, BottomBarBadge>(base.Element.Children.Count);
		for (int i = 0; i < base.Element.Children.Count; i++)
		{
			Page page = base.Element.Children[i];
			CreateOrUpdateBadgeForPage(page);
		}
	}

	private void AddPropertyChangedHandlersForPages()
	{
		foreach (Page child in base.Element.Children)
		{
			child.PropertyChanged += OnPagePropertyChanged;
		}
	}

	private void OnPagePropertyChanged(object sender, PropertyChangedEventArgs e)
	{
		if (e.PropertyName == BottomBarPageExtensions.BadgeCountProperty.PropertyName)
		{
			Page page = (Page)sender;
			CreateOrUpdateBadgeForPage(page);
		}
	}

	private void CreateOrUpdateBadgeForPage(Page page)
	{
		int tabPosition = base.Element.Children.IndexOf(page);
		int badgeCount = BottomBarPageExtensions.GetBadgeCount(page);
		BottomBarBadge bottomBarBadge;
		if (_badges.ContainsKey(page))
		{
			bottomBarBadge = _badges[page];
		}
		else
		{
			if (badgeCount == 0)
			{
				return;
			}
			Color badgeColor = BottomBarPageExtensions.GetBadgeColor(page);
			bottomBarBadge = _bottomBar.MakeBadgeForTabAt(tabPosition, badgeColor.ToAndroid(), badgeCount);
			_badges.Add(page, bottomBarBadge);
		}
		if (badgeCount == 0)
		{
			bottomBarBadge.Hide();
			return;
		}
		bottomBarBadge.Count = badgeCount;
		bottomBarBadge.Show();
	}
}
