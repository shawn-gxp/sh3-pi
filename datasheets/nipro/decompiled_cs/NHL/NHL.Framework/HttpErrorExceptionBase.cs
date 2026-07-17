using System;
using System.Net;

namespace NHL.Framework;

public abstract class HttpErrorExceptionBase : Exception
{
	public HttpStatusCode HttpStatusCode { get; set; }

	public HttpErrorExceptionBase(HttpStatusCode statusCode)
	{
		HttpStatusCode = statusCode;
	}

	public HttpErrorExceptionBase(HttpStatusCode statusCode, string message)
		: base(message)
	{
		HttpStatusCode = statusCode;
	}

	public HttpErrorExceptionBase(HttpStatusCode statusCode, string message, Exception innerException)
		: base(message, innerException)
	{
		HttpStatusCode = statusCode;
	}
}
