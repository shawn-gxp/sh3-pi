using System;
using System.Net;

namespace NHL.Framework;

public class HttpServerErrorException : HttpErrorExceptionBase
{
	public HttpServerErrorException(HttpStatusCode statusCode)
		: base(statusCode)
	{
	}

	public HttpServerErrorException(HttpStatusCode statusCode, string message)
		: base(statusCode, message)
	{
	}

	public HttpServerErrorException(HttpStatusCode statusCode, string message, Exception innerException)
		: base(statusCode, message, innerException)
	{
	}
}
