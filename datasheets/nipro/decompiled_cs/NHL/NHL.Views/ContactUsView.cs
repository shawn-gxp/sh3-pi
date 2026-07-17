using System;
using System.CodeDom.Compiler;
using System.Reflection;
using Caliburn.Micro.Xamarin.Forms;
using NHL.Views.Effect;
using Xamarin.Forms;
using Xamarin.Forms.Internals;
using Xamarin.Forms.Xaml;
using Xamarin.Forms.Xaml.Diagnostics;
using Xamarin.Forms.Xaml.Internals;

namespace NHL.Views;

[XamlCompilation(XamlCompilationOptions.Compile)]
[XamlFilePath("Views/ContactUsView.xaml")]
public class ContactUsView : ContentPage
{
	public ContactUsView()
	{
		InitializeComponent();
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private void InitializeComponent()
	{
		if (ResourceLoader.CanProvideContentFor(new ResourceLoader.ResourceLoadingQuery
		{
			AssemblyName = typeof(ContactUsView).GetTypeInfo().Assembly.GetName(),
			ResourcePath = "Views/ContactUsView.xaml",
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
		RowDefinition rowDefinition = new RowDefinition();
		RowDefinition rowDefinition2 = new RowDefinition();
		RowDefinition rowDefinition3 = new RowDefinition();
		RowDefinition rowDefinition4 = new RowDefinition();
		RowDefinition item = new RowDefinition();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect = new FixedFontSizeLabelEffect();
		Label label = new Label();
		BindingExtension bindingExtension = new BindingExtension();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect2 = new FixedFontSizeLabelEffect();
		Label label2 = new Label();
		FixedFontSizeButtonEffect fixedFontSizeButtonEffect = new FixedFontSizeButtonEffect();
		Button button = new Button();
		Grid grid = new Grid();
		ContactUsView contactUsView;
		NameScope value = (NameScope)(NameScope.GetNameScope(contactUsView = this) ?? new NameScope());
		NameScope.SetNameScope(contactUsView, value);
		contactUsView.SetValue(NavigationPage.BackButtonTitleProperty, "戻る");
		contactUsView.SetValue(Page.TitleProperty, "お問い合わせ");
		rowDefinition.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("50"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition);
		rowDefinition2.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition2);
		rowDefinition3.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("30"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition3);
		rowDefinition4.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("Auto"));
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition4);
		((DefinitionCollection<RowDefinition>)grid.GetValue(Grid.RowDefinitionsProperty)).Add(item);
		label.SetValue(Grid.RowProperty, 1);
		label.SetValue(Label.TextProperty, "以下にメールにてお問い合わせください。");
		label.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(10.0, 0.0, 0.0, 0.0));
		label.Effects.Add(fixedFontSizeLabelEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect, new Uri("Views/ContactUsView.xaml", UriKind.RelativeOrAbsolute), 21, 18);
		grid.Children.Add(label);
		VisualDiagnostics.RegisterSourceInfo(label, new Uri("Views/ContactUsView.xaml", UriKind.RelativeOrAbsolute), 19, 10);
		label2.SetValue(Grid.RowProperty, 3);
		bindingExtension.Path = "Email";
		BindingBase binding = ((IMarkupExtension<BindingBase>)bindingExtension).ProvideValue((IServiceProvider)null);
		label2.SetBinding(Label.TextProperty, binding);
		label2.SetValue(Label.TextColorProperty, Color.Blue);
		label2.SetValue(Xamarin.Forms.View.MarginProperty, new Thickness(10.0, 0.0, 0.0, 0.0));
		label2.Effects.Add(fixedFontSizeLabelEffect2);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect2, new Uri("Views/ContactUsView.xaml", UriKind.RelativeOrAbsolute), 27, 18);
		grid.Children.Add(label2);
		VisualDiagnostics.RegisterSourceInfo(label2, new Uri("Views/ContactUsView.xaml", UriKind.RelativeOrAbsolute), 25, 10);
		button.SetValue(Grid.RowProperty, 3);
		button.SetValue(Message.AttachProperty, "StartMailer");
		button.SetValue(VisualElement.BackgroundColorProperty, Color.Transparent);
		button.Effects.Add(fixedFontSizeButtonEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeButtonEffect, new Uri("Views/ContactUsView.xaml", UriKind.RelativeOrAbsolute), 32, 18);
		grid.Children.Add(button);
		VisualDiagnostics.RegisterSourceInfo(button, new Uri("Views/ContactUsView.xaml", UriKind.RelativeOrAbsolute), 30, 10);
		contactUsView.SetValue(ContentPage.ContentProperty, grid);
		VisualDiagnostics.RegisterSourceInfo(grid, new Uri("Views/ContactUsView.xaml", UriKind.RelativeOrAbsolute), 10, 6);
		VisualDiagnostics.RegisterSourceInfo(contactUsView, new Uri("Views/ContactUsView.xaml", UriKind.RelativeOrAbsolute), 2, 2);
	}

	private void __InitComponentRuntime()
	{
		this.LoadFromXaml(typeof(ContactUsView));
	}
}
