using System.Threading.Tasks;

namespace NHL.Services.DependencyService;

public interface IMediaPlayerService
{
	Task PlayAsync(string title);

	void Stop();
}
