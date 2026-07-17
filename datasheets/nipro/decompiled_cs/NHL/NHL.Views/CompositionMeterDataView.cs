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
[XamlFilePath("Views/CompositionMeterDataView.xaml")]
public class CompositionMeterDataView : ContentPage
{
	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private FlexGrid grid;

	public CompositionMeterDataView()
	{
		InitializeComponent();
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private void InitializeComponent()
	{
		if (ResourceLoader.CanProvideContentFor(new ResourceLoader.ResourceLoadingQuery
		{
			AssemblyName = typeof(CompositionMeterDataView).GetTypeInfo().Assembly.GetName(),
			ResourcePath = "Views/CompositionMeterDataView.xaml",
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
		GridColumn gridColumn3 = new GridColumn();
		GridColumn gridColumn4 = new GridColumn();
		FlexGrid flexGrid = new FlexGrid();
		Grid grid = new Grid();
		CompositionMeterDataView compositionMeterDataView;
		NameScope nameScope = (NameScope)(NameScope.GetNameScope(compositionMeterDataView = this) ?? new NameScope());
		NameScope.SetNameScope(compositionMeterDataView, nameScope);
		((INameScope)nameScope).RegisterName("grid", (object)flexGrid);
		if (flexGrid.StyleId == null)
		{
			flexGrid.StyleId = "grid";
		}
		this.grid = flexGrid;
		compositionMeterDataView.SetValue(NavigationPage.HasBackButtonProperty, true);
		compositionMeterDataView.SetValue(NavigationPage.BackButtonTitleProperty, "戻る");
		compositionMeterDataView.SetValue(Page.TitleProperty, "データ表");
		grid.SetValue(View.VerticalOptionsProperty, LayoutOptions.FillAndExpand);
		flexGrid.SetValue(Grid.RowProperty, 0);
		bindingExtension.Path = "DisplayCompChartData";
		BindingBase binding = ((IMarkupExtension<BindingBase>)bindingExtension).ProvideValue((IServiceProvider)null);
		flexGrid.SetBinding(FlexGrid.ItemsSourceProperty, binding);
		flexGrid.SetValue(FlexGrid.AutoGenerateColumnsProperty, false);
		flexGrid.SetValue(GridBase.HeadersVisibilityProperty, GridHeadersVisibility.Column);
		flexGrid.SetValue(View.VerticalOptionsProperty, LayoutOptions.FillAndExpand);
		flexGrid.SetValue(GridBase.SelectionModeProperty, GridSelectionMode.None);
		flexGrid.SetValue(View.HorizontalOptionsProperty, LayoutOptions.FillAndExpand);
		gridColumn.Binding = "MeasurementAt";
		gridColumn.Header = "日付";
		gridColumn.Format = "M/d";
		gridColumn.HeaderHorizontalAlignment = LayoutAlignment.Center;
		gridColumn.HorizontalAlignment = LayoutAlignment.Center;
		gridColumn.IsReadOnly = true;
		gridColumn.Width = (GridLength)new GridLengthTypeConverter().ConvertFromInvariantString("*");
		flexGrid.Columns.Add(gridColumn);
		gridColumn2.Binding = "Weight";
		gridColumn2.Header = "体重";
		gridColumn2.HeaderHorizontalAlignment = LayoutAlignment.Center;
		gridColumn2.HorizontalAlignment = LayoutAlignment.Center;
		gridColumn2.IsReadOnly = true;
		gridColumn2.Width = (GridLength)new GridLengthTypeConverter().ConvertFromInvariantString("*");
		flexGrid.Columns.Add(gridColumn2);
		gridColumn3.Binding = "Bmi";
		gridColumn3.Header = "BMI";
		gridColumn3.HeaderHorizontalAlignment = LayoutAlignment.Center;
		gridColumn3.HorizontalAlignment = LayoutAlignment.Center;
		gridColumn3.IsReadOnly = true;
		gridColumn3.Width = (GridLength)new GridLengthTypeConverter().ConvertFromInvariantString("*");
		flexGrid.Columns.Add(gridColumn3);
		gridColumn4.Binding = "FatPercentage";
		gridColumn4.Header = "体脂肪率";
		gridColumn4.HeaderHorizontalAlignment = LayoutAlignment.Center;
		gridColumn4.HorizontalAlignment = LayoutAlignment.Center;
		gridColumn4.IsReadOnly = true;
		gridColumn4.Width = (GridLength)new GridLengthTypeConverter().ConvertFromInvariantString("*");
		flexGrid.Columns.Add(gridColumn4);
		grid.Children.Add(flexGrid);
		VisualDiagnostics.RegisterSourceInfo(flexGrid, new Uri("Views/CompositionMeterDataView.xaml", UriKind.RelativeOrAbsolute), 11, 10);
		compositionMeterDataView.SetValue(ContentPage.ContentProperty, grid);
		VisualDiagnostics.RegisterSourceInfo(grid, new Uri("Views/CompositionMeterDataView.xaml", UriKind.RelativeOrAbsolute), 10, 6);
		VisualDiagnostics.RegisterSourceInfo(compositionMeterDataView, new Uri("Views/CompositionMeterDataView.xaml", UriKind.RelativeOrAbsolute), 2, 2);
	}

	private void __InitComponentRuntime()
	{
		this.LoadFromXaml(typeof(CompositionMeterDataView));
		grid = this.FindByName<FlexGrid>("grid");
	}
}
