using System;
using System.IO;
using System.Linq;
using System.Reflection;

namespace NHL.Droid.Renderer.Utils;

internal static class ResourceManagerEx
{
	internal static int IdFromTitle(string title, Type type)
	{
		string fileNameWithoutExtension = Path.GetFileNameWithoutExtension(title);
		return GetId(type, fileNameWithoutExtension);
	}

	private static int GetId(Type type, string propertyName)
	{
		FieldInfo fieldInfo = (from p in type.GetFields()
			select (p)).FirstOrDefault((FieldInfo p) => p.Name == propertyName);
		if (fieldInfo != null)
		{
			return (int)fieldInfo.GetValue(type);
		}
		return 0;
	}
}
