using System;

namespace NHL.ViewModels.Utils;

public static class FormatUtils
{
	public static double? ConvertKjToKCal(double? kj)
	{
		if (!kj.HasValue || kj <= 0.0)
		{
			return kj;
		}
		return Math.Round(kj.Value / 4.184, 1, MidpointRounding.AwayFromZero);
	}

	public static string ConvertExceedLimitTypeToDisplaySymbol(string exceedLimitType)
	{
		return exceedLimitType switch
		{
			"0" => string.Empty, 
			"1" => "↑", 
			"2" => "↓", 
			_ => string.Empty, 
		};
	}
}
