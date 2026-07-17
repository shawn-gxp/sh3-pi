using Plugin.BLE.Abstractions.Extensions;

namespace Plugin.BLE.Abstractions;

public class AdvertisementRecord
{
	public AdvertisementRecordType Type { get; private set; }

	public byte[] Data { get; private set; }

	public AdvertisementRecord(AdvertisementRecordType type, byte[] data)
	{
		Type = type;
		Data = data;
	}

	public override string ToString()
	{
		return string.Format("Adv rec [Type {0}; Data {1}]", new object[2]
		{
			Type,
			Data.ToHexString()
		});
	}
}
