using System;
using System.Net;

namespace NHL.Framework;

public class HttpClientErrorException : HttpErrorExceptionBase
{
	public HttpClientErrorException(HttpStatusCode statusCode)
		: base(statusCode)
	{
	}

	public HttpClientErrorException(HttpStatusCode statusCode, string message)
		: base(statusCode, message)
	{
	}

	public HttpClientErrorException(HttpStatusCode statusCode, string message, Exception innerExeption)
		: base(statusCode, message, innerExeption)
	{
	}
}
