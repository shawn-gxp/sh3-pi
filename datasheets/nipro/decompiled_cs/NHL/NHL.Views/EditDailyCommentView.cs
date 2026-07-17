using System;
using System.CodeDom.Compiler;
using System.Reflection;
using AiForms.Effects;
using NHL.Views.Converters;
using NHL.Views.Effect;
using Xamarin.Forms;
using Xamarin.Forms.Internals;
using Xamarin.Forms.Xaml;
using Xamarin.Forms.Xaml.Diagnostics;
using Xamarin.Forms.Xaml.Internals;

namespace NHL.Views;

[XamlCompilation(XamlCompilationOptions.Compile)]
[XamlFilePath("Views/EditDailyCommentView.xaml")]
public class EditDailyCommentView : ContentPage
{
	public EditDailyCommentView()
	{
		InitializeComponent();
	}

	[GeneratedCode("Xamarin.Forms.Build.Tasks.XamlG", "2.0.0.0")]
	private void InitializeComponent()
	{
		if (ResourceLoader.CanProvideContentFor(new ResourceLoader.ResourceLoadingQuery
		{
			AssemblyName = typeof(EditDailyCommentView).GetTypeInfo().Assembly.GetName(),
			ResourcePath = "Views/EditDailyCommentView.xaml",
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
		BooleanSwitchConverter value = new BooleanSwitchConverter();
		Color color = Color.FromHex("#999999");
		ResourceDictionary resourceDictionary = new ResourceDictionary();
		OnPlatform<Thickness> onPlatform = new OnPlatform<Thickness>();
		RowDefinition item = new RowDefinition();
		RowDefinition item2 = new RowDefinition();
		RowDefinition rowDefinition = new RowDefinition();
		BindingExtension bindingExtension = new BindingExtension();
		FixedFontSizeLabelEffect fixedFontSizeLabelEffect = new FixedFontSizeLabelEffect();
		Label label = new Label();
		BindingExtension bindingExtension2 = new BindingExtension();
		Editor editor = new Editor();
		Grid grid = new Grid();
		ScrollView scrollView = new ScrollView();
		Grid grid2 = new Grid();
		EditDailyCommentView editDailyCommentView;
		NameScope nameScope = (NameScope)(NameScope.GetNameScope(editDailyCommentView = this) ?? new NameScope());
		NameScope.SetNameScope(editDailyCommentView, nameScope);
		editDailyCommentView.Resources = resourceDictionary;
		VisualDiagnostics.RegisterSourceInfo(resourceDictionary, new Uri("Views/EditDailyCommentView.xaml", UriKind.RelativeOrAbsolute), 17, 10);
		resourceDictionary.Add("BooleanSwitchConverter", value);
		resourceDictionary.Add("InputFrameColor", color);
		editDailyCommentView.SetValue(NavigationPage.HasBackButtonProperty, true);
		editDailyCommentView.SetValue(NavigationPage.BackButtonTitleProperty, "戻る");
		editDailyCommentView.SetValue(Page.TitleProperty, "");
		editDailyCommentView.Resources = resourceDictionary;
		VisualDiagnostics.RegisterSourceInfo(resourceDictionary, new Uri("Views/EditDailyCommentView.xaml", UriKind.RelativeOrAbsolute), 17, 10);
		onPlatform.iOS = new Thickness(20.0, 30.0, 20.0, 20.0);
		onPlatform.Android = new Thickness(20.0, 30.0, 20.0, 20.0);
		editDailyCommentView.SetValue(Page.PaddingProperty, (Thickness)onPlatform);
		VisualDiagnostics.RegisterSourceInfo(onPlatform, new Uri("Views/EditDailyCommentView.xaml", UriKind.RelativeOrAbsolute), 28, 10);
		grid2.SetValue(Grid.RowSpacingProperty, 0.0);
		((DefinitionCollection<RowDefinition>)grid2.GetValue(Grid.RowDefinitionsProperty)).Add(item);
		((DefinitionCollection<RowDefinition>)grid2.GetValue(Grid.RowDefinitionsProperty)).Add(item2);
		rowDefinition.SetValue(RowDefinition.HeightProperty, new GridLengthTypeConverter().ConvertFromInvariantString("85"));
		((DefinitionCollection<RowDefinition>)grid2.GetValue(Grid.RowDefinitionsProperty)).Add(rowDefinition);
		scrollView.SetValue(Grid.RowProperty, 0);
		BindableProperty fontSizeProperty = Label.FontSizeProperty;
		FontSizeConverter fontSizeConverter = new FontSizeConverter();
		XamlServiceProvider xamlServiceProvider = new XamlServiceProvider();
		Type typeFromHandle = typeof(IProvideValueTarget);
		object[] array = new object[0 + 5];
		array[0] = label;
		array[1] = grid;
		array[2] = scrollView;
		array[3] = grid2;
		array[4] = editDailyCommentView;
		object service;
		xamlServiceProvider.Add(typeFromHandle, service = new SimpleValueTargetProvider(array, Label.FontSizeProperty, nameScope));
		xamlServiceProvider.Add(typeof(IReferenceProvider), service);
		Type typeFromHandle2 = typeof(IXamlTypeResolver);
		XmlNamespaceResolver xmlNamespaceResolver = new XmlNamespaceResolver();
		xmlNamespaceResolver.Add("", "http://xamarin.com/schemas/2014/forms");
		xmlNamespaceResolver.Add("x", "http://schemas.microsoft.com/winfx/2009/xaml");
		xmlNamespaceResolver.Add("calx", "clr-namespace:Caliburn.Micro.Xamarin.Forms;assembly=Caliburn.Micro.Platform.Xamarin.Forms");
		xmlNamespaceResolver.Add("carousel", "clr-namespace:CarouselView.FormsPlugin.Abstractions;assembly=CarouselView.FormsPlugin.Abstractions");
		xmlNamespaceResolver.Add("ef", "clr-namespace:AiForms.Effects;assembly=AiForms.Effects");
		xmlNamespaceResolver.Add("converter", "clr-namespace:NHL.Views.Converters");
		xmlNamespaceResolver.Add("effect", "clr-namespace:NHL.Views.Effect");
		xmlNamespaceResolver.Add("nhl", "clr-namespace:NHL.Views.Controls");
		xmlNamespaceResolver.Add("renderer", "clr-namespace:NHL.Views.Renderer");
		xamlServiceProvider.Add(typeFromHandle2, new XamlTypeResolver(xmlNamespaceResolver, typeof(EditDailyCommentView).GetTypeInfo().Assembly));
		xamlServiceProvider.Add(typeof(IXmlLineInfoProvider), new XmlLineInfoProvider(new XmlLineInfo(43, 28)));
		label.SetValue(fontSizeProperty, ((IExtendedTypeConverter)fontSizeConverter).ConvertFromInvariantString("Large", (IServiceProvider)xamlServiceProvider));
		bindingExtension.Path = "CommentFlag";
		BindingBase binding = ((IMarkupExtension<BindingBase>)bindingExtension).ProvideValue((IServiceProvider)null);
		label.SetBinding(VisualElement.IsVisibleProperty, binding);
		label.SetValue(View.MarginProperty, new Thickness(4.0, 5.0, 0.0, 0.0));
		label.SetValue(Label.TextColorProperty, Color.Gray);
		label.SetValue(Label.TextProperty, "コメントを入力");
		label.Effects.Add(fixedFontSizeLabelEffect);
		VisualDiagnostics.RegisterSourceInfo(fixedFontSizeLabelEffect, new Uri("Views/EditDailyCommentView.xaml", UriKind.RelativeOrAbsolute), 45, 26);
		grid.Children.Add(label);
		VisualDiagnostics.RegisterSourceInfo(label, new Uri("Views/EditDailyCommentView.xaml", UriKind.RelativeOrAbsolute), 43, 22);
		bindingExtension2.Path = "Comment";
		BindingBase binding2 = ((IMarkupExtension<BindingBase>)bindingExtension2).ProvideValue((IServiceProvider)null);
		editor.SetBinding(Editor.TextProperty, binding2);
		editor.SetValue(InputView.MaxLengthProperty, 500);
		editor.SetValue(InputView.KeyboardProperty, new KeyboardTypeConverter().ConvertFromInvariantString("Chat"));
		editor.SetValue(InputView.IsSpellCheckEnabledProperty, false);
		editor.SetValue(VisualElement.BackgroundColorProperty, Color.Transparent);
		editor.SetValue(Border.OnProperty, true);
		editor.SetValue(Border.ColorProperty, Color.Black);
		editor.SetValue(Border.WidthProperty, 1.0);
		grid.Children.Add(editor);
		VisualDiagnostics.RegisterSourceInfo(editor, new Uri("Views/EditDailyCommentView.xaml", UriKind.RelativeOrAbsolute), 48, 22);
		scrollView.Content = grid;
		VisualDiagnostics.RegisterSourceInfo(grid, new Uri("Views/EditDailyCommentView.xaml", UriKind.RelativeOrAbsolute), 42, 18);
		grid2.Children.Add(scrollView);
		VisualDiagnostics.RegisterSourceInfo(scrollView, new Uri("Views/EditDailyCommentView.xaml", UriKind.RelativeOrAbsolute), 40, 10);
		editDailyCommentView.SetValue(ContentPage.ContentProperty, grid2);
		VisualDiagnostics.RegisterSourceInfo(grid2, new Uri("Views/EditDailyCommentView.xaml", UriKind.RelativeOrAbsolute), 33, 6);
		VisualDiagnostics.RegisterSourceInfo(editDailyCommentView, new Uri("Views/EditDailyCommentView.xaml", UriKind.RelativeOrAbsolute), 2, 2);
	}

	private void __InitComponentRuntime()
	{
		this.LoadFromXaml(typeof(EditDailyCommentView));
	}
}
