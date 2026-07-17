using System;

namespace NHL.Services.DependencyService;

public interface IWebBrowserService
{
	void Open(Uri uri);
}
