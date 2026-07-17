using System;
using System.Net;

namespace NHL.Framework;

public class HttpUnauthorizedErrorException : HttpErrorExceptionBase
{
	public HttpUnauthorizedErrorException()
		: base(HttpStatusCode.Unauthorized)
	{
	}

	public HttpUnauthorizedErrorException(string message)
		: base(HttpStatusCode.Unauthorized, message)
	{
	}

	public HttpUnauthorizedErrorException(string message, Exception innerException)
		: base(HttpStatusCode.Unauthorized, message, innerException)
	{
	}
}
