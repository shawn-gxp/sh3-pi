using System;
using System.Linq;
using System.Threading.Tasks;
using Android.App;
using Microsoft.IdentityModel.Clients.ActiveDirectory;
using NHL.Services.DependencyService;
using Xamarin.Forms;

namespace NHL.Droid.DependencyService;

public class Authenticator : IAuthenticator
{
	public async Task<AuthenticationResult> Authenticate(string authority, string resource, string clientId, string returnUri)
	{
		AuthenticationContext authenticationContext = new AuthenticationContext(authority);
		if (authenticationContext.TokenCache.ReadItems().Any())
		{
			authenticationContext = new AuthenticationContext(authenticationContext.TokenCache.ReadItems().First().Authority);
		}
		Uri redirectUri = new Uri(returnUri);
		PlatformParameters parameters = new PlatformParameters((Activity)Forms.Context);
		return await authenticationContext.AcquireTokenAsync(resource, clientId, redirectUri, parameters);
	}
}
