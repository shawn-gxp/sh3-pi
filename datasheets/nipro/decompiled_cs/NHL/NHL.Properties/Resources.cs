using System.CodeDom.Compiler;
using System.ComponentModel;
using System.Diagnostics;
using System.Globalization;
using System.Reflection;
using System.Resources;
using System.Runtime.CompilerServices;

namespace NHL.Properties;

[GeneratedCode("System.Resources.Tools.StronglyTypedResourceBuilder", "16.0.0.0")]
[DebuggerNonUserCode]
[CompilerGenerated]
internal class Resources
{
	private static ResourceManager resourceMan;

	private static CultureInfo resourceCulture;

	[EditorBrowsable(EditorBrowsableState.Advanced)]
	internal static ResourceManager ResourceManager
	{
		get
		{
			if (resourceMan == null)
			{
				resourceMan = new ResourceManager("NHL.Properties.Resources", typeof(Resources).GetTypeInfo().Assembly);
			}
			return resourceMan;
		}
	}

	[EditorBrowsable(EditorBrowsableState.Advanced)]
	internal static CultureInfo Culture
	{
		get
		{
			return resourceCulture;
		}
		set
		{
			resourceCulture = value;
		}
	}

	internal static string ALERT_DIALOG_TITLE => ResourceManager.GetString("ALERT_DIALOG_TITLE", resourceCulture);

	internal static string DEFAULT_WAIT_MESSAGE => ResourceManager.GetString("DEFAULT_WAIT_MESSAGE", resourceCulture);

	internal static string FACILITIES_NAME_WHEN_HOSPITAL_NOT_SET => ResourceManager.GetString("FACILITIES_NAME_WHEN_HOSPITAL_NOT_SET", resourceCulture);

	internal static string MESSAGE_001 => ResourceManager.GetString("MESSAGE_001", resourceCulture);

	internal static string MESSAGE_002 => ResourceManager.GetString("MESSAGE_002", resourceCulture);

	internal static string MESSAGE_003 => ResourceManager.GetString("MESSAGE_003", resourceCulture);

	internal static string MESSAGE_004 => ResourceManager.GetString("MESSAGE_004", resourceCulture);

	internal static string MESSAGE_005 => ResourceManager.GetString("MESSAGE_005", resourceCulture);

	internal static string MESSAGE_006 => ResourceManager.GetString("MESSAGE_006", resourceCulture);

	internal static string MESSAGE_007 => ResourceManager.GetString("MESSAGE_007", resourceCulture);

	internal static string MESSAGE_008 => ResourceManager.GetString("MESSAGE_008", resourceCulture);

	internal static string MESSAGE_009 => ResourceManager.GetString("MESSAGE_009", resourceCulture);

	internal static string MESSAGE_010 => ResourceManager.GetString("MESSAGE_010", resourceCulture);

	internal static string MESSAGE_011 => ResourceManager.GetString("MESSAGE_011", resourceCulture);

	internal static string MESSAGE_012 => ResourceManager.GetString("MESSAGE_012", resourceCulture);

	internal static string MESSAGE_013 => ResourceManager.GetString("MESSAGE_013", resourceCulture);

	internal static string MESSAGE_014 => ResourceManager.GetString("MESSAGE_014", resourceCulture);

	internal Resources()
	{
	}
}
