using System.Collections.Generic;

namespace NHL.Services.DependencyService;

public interface IMailService
{
	bool StartMailer(List<string> to);
}
