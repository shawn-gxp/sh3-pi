using System;
using System.Collections.Generic;

namespace BLELib.BLEDevice.Record;

public class BloodPressureResult
{
	public class BloodPressureMeasureRecord
	{
		public DateTime TimeStamp { get; set; }

		public double SBP { get; set; }

		public double DBP { get; set; }

		public double MAP { get; set; }

		public double PulseRate { get; set; }

		public override string ToString()
		{
			return string.Format(TimeStamp.ToString() + ", " + SBP + ", " + DBP + ", " + MAP + ", " + PulseRate);
		}
	}

	public IList<BloodPressureMeasureRecord> Result { get; set; }
}
