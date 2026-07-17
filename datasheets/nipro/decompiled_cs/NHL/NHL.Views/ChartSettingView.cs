using System;
using System.CodeDom.Compiler;
using System.Reflection;
using C1.Xamarin.Forms.Grid;
using Xamarin.Forms;
using Xamarin.Forms.Internals;
using Xamarin.Forms.Xaml;
using Xamarin.Forms.Xaml.Diagnostics;
using Xamarin.Forms.Xaml.Internals;

namespace NHL.Views;

[XamlCompilation(XamlCompilationOptions.Compile)]
[XamlFilePath("Views/ChartSettingView.xaml")]
public class ChartSettingView : ContentPage
{
	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private FlexGrid grid;

	public ChartSettingView()
	{
		InitializeComponent();
	}

	protected override void OnDisappearing()
	{
		grid.FinishEditing();
		base.OnDisappearing();
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private void InitializeComponent()
	{
		if (ResourceLoader.CanProvideContentFor(new ResourceLoader.ResourceLoadingQuery
		{
			AssemblyName = typeof(ChartSettingView).GetTypeInfo().Assembly.GetName(),
			ResourcePath = "Views/ChartSettingView.xaml",
			Instance = this
		}))
		{
			__InitComponentRuntime();
			return;
		}
		if (XamlLoader.XamlFileProvider != null && XamlLoader.XamlFileProvider(GetType()) != null)
		{
			__InitComponentRuntime();
			return;
		}
		BindingExtension bindingExtension = new BindingExtension();
		GridColumn gridColumn = new GridColumn();
		GridColumn gridColumn2 = new GridColumn();
		FlexGrid flexGrid = new FlexGrid();
		Grid grid = new Grid();
		StackLayout stackLayout = new StackLayout();
		ChartSettingView chartSettingView;
		NameScope nameScope = (NameScope)(NameScope.GetNameScope(chartSettingView = this) ?? new NameScope());
		NameScope.SetNameScope(chartSettingView, nameScope);
		((INameScope)nameScope).RegisterName("grid", (object)flexGrid);
		if (flexGrid.StyleId == null)
		{
			flexGrid.StyleId = "grid";
		}
		this.grid = flexGrid;
		chartSettingView.SetValue(NavigationPage.HasBackButtonProperty, true);
		chartSettingView.SetValue(NavigationPage.BackButtonTitleProperty, "戻る");
		chartSettingView.SetValue(Page.TitleProperty, "表示項目設定");
		grid.SetValue(View.VerticalOptionsProperty, LayoutOptions.FillAndExpand);
		grid.SetValue(View.HorizontalOptionsProperty, LayoutOptions.FillAndExpand);
		flexGrid.SetValue(GridBase.HeadersVisibilityProperty, GridHeadersVisibility.None);
		bindingExtension.Path = "ChartSettings";
		BindingBase binding = ((IMarkupExtension<BindingBase>)bindingExtension).ProvideValue((IServiceProvider)null);
		flexGrid.SetBinding(FlexGrid.ItemsSourceProperty, binding);
		flexGrid.SetValue(FlexGrid.AutoGenerateColumnsProperty, false);
		flexGrid.SetValue(GridBase.SelectionModeProperty, GridSelectionMode.None);
		flexGrid.SetValue(View.HorizontalOptionsProperty, LayoutOptions.FillAndExpand);
		flexGrid.SetValue(View.VerticalOptionsProperty, LayoutOptions.Start);
		gridColumn.Binding = "Setting";
		gridColumn.IsReadOnly = true;
		gridColumn.Width = (GridLength)new GridLengthTypeConverter().ConvertFromInvariantString("*");
		flexGrid.Columns.Add(gridColumn);
		gridColumn2.Binding = "Active";
		gridColumn2.HorizontalAlignment = LayoutAlignment.Center;
		gridColumn2.Width = (GridLength)new GridLengthTypeConverter().ConvertFromInvariantString("60");
		flexGrid.Columns.Add(gridColumn2);
		grid.Children.Add(flexGrid);
		VisualDiagnostics.RegisterSourceInfo(flexGrid, new Uri("Views/ChartSettingView.xaml", UriKind.RelativeOrAbsolute), 14, 14);
		stackLayout.Children.Add(grid);
		VisualDiagnostics.RegisterSourceInfo(grid, new Uri("Views/ChartSettingView.xaml", UriKind.RelativeOrAbsolute), 12, 10);
		chartSettingView.SetValue(ContentPage.ContentProperty, stackLayout);
		VisualDiagnostics.RegisterSourceInfo(stackLayout, new Uri("Views/ChartSettingView.xaml", UriKind.RelativeOrAbsolute), 11, 6);
		VisualDiagnostics.RegisterSourceInfo(chartSettingView, new Uri("Views/ChartSettingView.xaml", UriKind.RelativeOrAbsolute), 3, 2);
	}

	private void __InitComponentRuntime()
	{
		this.LoadFromXaml(typeof(ChartSettingView));
		grid = this.FindByName<FlexGrid>("grid");
	}
}
