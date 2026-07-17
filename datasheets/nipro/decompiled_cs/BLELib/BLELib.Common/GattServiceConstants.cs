namespace BLELib.Common;

public sealed class GattServiceConstants
{
	public sealed class DeviceInformationServiceConstants
	{
		public const string SERVICE_UUID = "0000180A-0000-1000-8000-00805F9B34FB";

		public const string SYSTEM_ID = "00002A23-0000-1000-8000-00805F9B34FB";

		public const string MODEL_NUMBER = "00002A24-0000-1000-8000-00805F9B34FB";

		public const string SERIAL_NUMBER = "00002A25-0000-1000-8000-00805F9B34FB";

		public const string FIRMWARE_REVISION = "00002A26-0000-1000-8000-00805F9B34FB";

		public const string HARDWARE_REVISION = "00002A27-0000-1000-8000-00805F9B34FB";

		public const string SOFTWARE_REVISION = "00002A28-0000-1000-8000-00805F9B34FB";

		public const string MANUFACTURE_NAME = "00002A29-0000-1000-8000-00805F9B34FB";

		public const string REGISTRATION_CERTIFICATION_DATA = "00002A2A-0000-1000-8000-00805F9B34FB";
	}

	public sealed class BatteryServiceConstants
	{
		public const string SERVICE_UUID = "0000180F-0000-1000-8000-00805F9B34FB";

		public const string BATTERY_LEVEL = "00002A19-0000-1000-8000-00805F9B34FB";
	}

	public sealed class WeightScaleServiceConstants
	{
		public const string SERVICE_UUID = "0000181D-0000-1000-8000-00805F9B34FB";

		public const string WEIGHT_SCALE_MEASUREMENT = "00002A9D-0000-1000-8000-00805F9B34FB";

		public const string WEIGHT_SCALE_FEATURE = "00002A9E-0000-1000-8000-00805F9B34FB";
	}

	public sealed class BodyCompositionServiceConstants
	{
		public const string SERVICE_UUID = "0000181B-0000-1000-8000-00805F9B34FB";

		public const string BODY_COMPOSITION_MEASUREMENT = "00002A9C-0000-1000-8000-00805F9B34FB";

		public const string BODY_COMPOSITION_FEATURE = "0000292B-0000-1000-8000-00805F9B34FB";
	}

	public sealed class CurrentTimeServiceConstants
	{
		public const string SERVICE_UUID = "00001805-0000-1000-8000-00805F9B34FB";

		public const string CURRENT_TIME = "00002A2B-0000-1000-8000-00805F9B34FB";

		public const string LOCAL_TIME_INFORMATION = "00002A0F-0000-1000-8000-00805F9B34FB";
	}

	public sealed class UserDataServiceConstants
	{
		public const string SERVICE_UUID = "0000181C-0000-1000-8000-00805F9B34FB";

		public const string USER_CONTROL_POINT = "00002A9F-0000-1000-8000-00805F9B34FB";

		public const string FIRST_NAME = "00002A8A-0000-1000-8000-00805F9B34FB";

		public const string LAST_NAME = "00002A90-0000-1000-8000-00805F9B34FB";

		public const string E_MAIL = "00002A87-0000-1000-8000-00805F9B34FB";

		public const string BIRTHDAY = "00002A85-0000-1000-8000-00805F9B34FB";

		public const string SEX = "00002A8C-0000-1000-8000-00805F9B34FB";

		public const string HEIGHT = "00002A8E-0000-1000-8000-00805F9B34FB";
	}

	public sealed class BodyCompositionAAndDCustomServiceConstants
	{
		public const string SERVICE_UUID = "11127000-B364-11E4-AB27-0800200C9A66";

		public const string WRITE_REQ = "11127001-B364-11E4-AB27-0800200C9A66";

		public const string NOTIFICATION = "11127002-B364-11E4-AB27-0800200C9A66";
	}

	public const string COMMON_UUID_TAIL = "-0000-1000-8000-00805F9B34FB";

	public const string CCCD_UUID = "00002902-0000-1000-8000-00805f9b34fb";
}
