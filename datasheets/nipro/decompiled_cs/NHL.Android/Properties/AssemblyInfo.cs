using System.Diagnostics;
using System.Reflection;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;
using System.Runtime.Versioning;
using Android.App;
using Android.Runtime;
using NHL.Droid.DependencyService;
using NHL.Droid.Effect;
using NHL.Droid.Renderer;
using NHL.Views.Renderer;
using Xamarin.Forms;
using blood.Droid.DependencyService;

[assembly: Dependency(typeof(AndroidSnackBar))]
[assembly: Dependency(typeof(AssemblyService))]
[assembly: Dependency(typeof(Authenticator))]
[assembly: Dependency(typeof(BLELibService))]
[assembly: Dependency(typeof(BusyIndicator))]
[assembly: Dependency(typeof(CheckPermissionService))]
[assembly: Dependency(typeof(ClipBoard))]
[assembly: Dependency(typeof(CurrentOrientationService))]
[assembly: Dependency(typeof(FileControlService))]
[assembly: Dependency(typeof(IdentifierForVendorService))]
[assembly: Dependency(typeof(ImageService))]
[assembly: Dependency(typeof(LoggingService))]
[assembly: Dependency(typeof(MailService))]
[assembly: Dependency(typeof(MediaPlayerService))]
[assembly: Dependency(typeof(ApplicationSleepControlService))]
[assembly: Dependency(typeof(NetworkService))]
[assembly: Dependency(typeof(RequestBluetooth))]
[assembly: Dependency(typeof(RequestLocation))]
[assembly: Dependency(typeof(SQLite_Android))]
[assembly: Dependency(typeof(ThreadInformationService))]
[assembly: Dependency(typeof(WebBrowserService))]
[assembly: ExportEffect(typeof(FixedFontSizeButtonEffect), "FixedFontSizeButtonEffect")]
[assembly: ExportEffect(typeof(FixedFontSizeLabelEffect), "FixedFontSizeLabelEffect")]
[assembly: ExportEffect(typeof(FixedFontSizeTextBoxEffect), "FixedFontSizeTextBoxEffect")]
[assembly: ExportEffect(typeof(FlatEntryEffect), "FlatEntryEffect")]
[assembly: ExportEffect(typeof(FlatPickerEffect), "FlatPickerEffect")]
[assembly: ResolutionGroupName("GxP")]
[assembly: ExportEffect(typeof(UnderlineEffect), "UnderlineEffect")]
[assembly: UsesFeature("android.hardware.camera", Required = false)]
[assembly: UsesFeature("android.hardware.camera.autofocus", Required = false)]
[assembly: ExportRenderer(typeof(DatePickerView), typeof(DatePickerViewRenderer))]
[assembly: ExportRenderer(typeof(HeightPickerView), typeof(HeightPickerViewRenderer))]
[assembly: ExportRenderer(typeof(WebView), typeof(NoCachedWebViewRenderer))]
[assembly: ExportRenderer(typeof(RadiusBoxView), typeof(RadiusBoxViewRenderer))]
[assembly: ExportRenderer(typeof(TimeZoneOtherPicker), typeof(TimeZoneOtherPickerRenderer))]
[assembly: ExportRenderer(typeof(TimeZonePicker), typeof(TimeZonePickerRenderer))]
[assembly: ResourceDesigner("NHL.Droid.Resource", IsApplication = true)]
[assembly: AssemblyTitle("NHL.Android")]
[assembly: AssemblyDescription("")]
[assembly: AssemblyConfiguration("")]
[assembly: AssemblyCompany("")]
[assembly: AssemblyProduct("NHL.Android")]
[assembly: AssemblyCopyright("Copyright ©  2014")]
[assembly: AssemblyTrademark("")]
[assembly: ComVisible(false)]
[assembly: AssemblyFileVersion("1.0.0.0")]
[assembly: UsesPermission("android.permission.INTERNET")]
[assembly: UsesPermission("android.permission.WRITE_EXTERNAL_STORAGE")]
[assembly: ExportRenderer(typeof(PickerView), typeof(PickerViewRenderer))]
[assembly: ExportRenderer(typeof(BottomBarPage), typeof(NHLBottomBarPageRenderer))]
[assembly: ExportRenderer(typeof(ExtendedButton), typeof(ExtendedButtonRenderer))]
[assembly: AssemblyVersion("1.0.0.0")]
