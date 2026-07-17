using PCLStorage;

namespace NHL.Services.DependencyService;

public interface IFileControlService
{
	long GetFileSize(IFile file);
}
