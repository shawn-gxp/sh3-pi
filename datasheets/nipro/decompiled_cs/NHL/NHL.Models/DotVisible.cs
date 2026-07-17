using NHL.Models.Support;

namespace NHL.Models;

public class DotVisible : ModelBase
{
	public int Day { get; set; }

	public bool GlucoseDotVisible { get; set; }

	public bool TemperatureDotVisible { get; set; }

	public bool SphygmomanometerDotVisible { get; set; }

	public bool CompositionMeterDotVisible { get; set; }

	public bool StepMeterDotVisible { get; set; }
}
