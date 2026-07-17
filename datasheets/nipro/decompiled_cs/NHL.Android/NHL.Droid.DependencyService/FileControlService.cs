using System.IO;
using NHL.Services.DependencyService;
using PCLStorage;

namespace NHL.Droid.DependencyService;

public class FileControlService : IFileControlService
{
	public long GetFileSize(IFile file)
	{
		return new FileInfo(file.Path).Length;
	}
}
