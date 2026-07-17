using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using NHL.Common;
using NHL.Models;
using NHL.Services.Support;
using NHL.ViewModels.Utils;
using Newtonsoft.Json;

namespace NHL.Services;

public class UserManager : IUserManager
{
	public string AuthenticationToken { get; set; }

	public IgUserModel IgUser { get; set; }

	public HospitalModel Hospital { get; set; }

	public IEnumerable<HospitalModel> Hospitals { get; set; }

	public LoginModel SharerLogin { get; set; }

	public HospitalModel SharerHospital { get; set; }

	public bool IsExistingUser { get; set; }

	public IAuthenticatedUserService AuthenticatedUserService { get; set; }

	public IAuthenticationService AuthenticationService { get; set; }

	public IHospitalInfoService HospitalInfoService { get; set; }

	public IIgUserService IgUserService { get; set; }

	public ILinkHospitalService LinkHospitalService { get; set; }

	public IUnlinkHospitalService UnlinkHospitalService { get; set; }

	public IUpdateTimezoneService UpdateTimezoneService { get; set; }

	public IUpdateIgUserService UpdateIgUserService { get; set; }

	public IUpdateIgPasswordService UpdateIgPasswordService { get; set; }

	public ILogfileTransferService LogfileTransferService { get; set; }

	public IUpdateIgGlucoseTargetService UpdateIgGlucoseTargetService { get; set; }

	public ISharerRegisterService SharerRegisterService { get; set; }

	public ISharerUpdateService SharerUpdateService { get; set; }

	public IUpdateTimezoneOtherService UpdateTimezoneOtherService { get; set; }

	public IVersionService VersionService { get; set; }

	public void Initialize(IAuthenticatedUserService authenticatedUserService, IAuthenticationService authenticationService, IHospitalInfoService hospitalInfoService, IIgUserService igUserService, ILinkHospitalService linkHospitalService, IUnlinkHospitalService unlinkHospitalService, IUpdateTimezoneService updateTimezoneService, IUpdateIgUserService updateIgUserService, IUpdateIgPasswordService updateIgPasswordService, ILogfileTransferService logfileTransferService, IUpdateIgGlucoseTargetService updateIgGlucoseTargetService, ISharerRegisterService sharerRegisterService, ISharerUpdateService sharerUpdateService, IUpdateTimezoneOtherService updateTimezoneOtherService, IVersionService versionService)
	{
		AuthenticatedUserService = authenticatedUserService;
		AuthenticationService = authenticationService;
		HospitalInfoService = hospitalInfoService;
		IgUserService = igUserService;
		LinkHospitalService = linkHospitalService;
		UnlinkHospitalService = unlinkHospitalService;
		UpdateTimezoneService = updateTimezoneService;
		UpdateIgUserService = updateIgUserService;
		UpdateIgPasswordService = updateIgPasswordService;
		LogfileTransferService = logfileTransferService;
		UpdateIgGlucoseTargetService = updateIgGlucoseTargetService;
		SharerRegisterService = sharerRegisterService;
		SharerUpdateService = sharerUpdateService;
		UpdateTimezoneOtherService = updateTimezoneOtherService;
		VersionService = versionService;
	}

	public async Task<bool> RegistUser(IgUserRequestModel igUserRequestModel)
	{
		bool bRes = false;
		try
		{
			IgUser = await IgUserService.IgUser(igUserRequestModel);
			bRes = true;
		}
		catch (Exception)
		{
		}
		return bRes;
	}

	public async Task<bool> RestoreUser(string backupCode, string backupPassword)
	{
		bool bRes = false;
		try
		{
			IgUserRestoreRequestModel backupCode2 = new IgUserRestoreRequestModel
			{
				BackupCode = backupCode,
				BackupPassword = backupPassword
			};
			IgUser = await IgUserService.RestoreIgUser(backupCode2);
			bRes = true;
		}
		catch (Exception)
		{
		}
		return bRes;
	}

	public async Task<bool> Authenticate()
	{
		bool bRes = false;
		try
		{
			string idfv = NHL.Common.Common.GetIdfv();
			AuthenticationRequestModel auth = new AuthenticationRequestModel
			{
				IgUserId = IgUser.Id,
				Idfv = idfv
			};
			AuthenticationModel authenticationModel = await AuthenticationService.Authenticate(auth);
			if (!string.IsNullOrEmpty(authenticationModel?.AuthenticationToken))
			{
				AuthenticationToken = authenticationModel.AuthenticationToken;
				IgUser = authenticationModel.IgUser;
				Hospital = authenticationModel.Hospital;
				Hospitals = authenticationModel.Hospitals;
				SharerLogin = authenticationModel.SharerLogin;
				SharerHospital = authenticationModel.SharerHospital;
				SyncManager.GetInstance().Token = AuthenticationToken;
				AuthenticatedUserService.Token = AuthenticationToken;
				AuthenticationService.Token = AuthenticationToken;
				HospitalInfoService.Token = AuthenticationToken;
				IgUserService.Token = AuthenticationToken;
				LinkHospitalService.Token = AuthenticationToken;
				UnlinkHospitalService.Token = AuthenticationToken;
				UpdateTimezoneService.Token = AuthenticationToken;
				UpdateIgUserService.Token = AuthenticationToken;
				LogfileTransferService.Token = AuthenticationToken;
				UpdateIgGlucoseTargetService.Token = AuthenticationToken;
				SharerRegisterService.Token = AuthenticationToken;
				SharerUpdateService.Token = AuthenticationToken;
				UpdateTimezoneOtherService.Token = AuthenticationToken;
				VersionService.Token = AuthenticationToken;
				bRes = true;
			}
		}
		catch (Exception)
		{
		}
		return bRes;
	}

	public async Task Authenticate(string token)
	{
		AuthenticationToken = token;
		SyncManager.GetInstance().Token = AuthenticationToken;
		AuthenticatedUserService.Token = AuthenticationToken;
		AuthenticationService.Token = AuthenticationToken;
		HospitalInfoService.Token = AuthenticationToken;
		IgUserService.Token = AuthenticationToken;
		LinkHospitalService.Token = AuthenticationToken;
		UnlinkHospitalService.Token = AuthenticationToken;
		UpdateTimezoneService.Token = AuthenticationToken;
		UpdateIgUserService.Token = AuthenticationToken;
		LogfileTransferService.Token = AuthenticationToken;
		UpdateIgGlucoseTargetService.Token = AuthenticationToken;
		SharerRegisterService.Token = AuthenticationToken;
		SharerUpdateService.Token = AuthenticationToken;
		UpdateTimezoneOtherService.Token = AuthenticationToken;
		await Task.Delay(1);
	}

	public async Task<bool> LoadUserModel()
	{
		try
		{
			string value = await IsolatedStorageUtils.LoadAsync("login.txt");
			if (!string.IsNullOrEmpty(value))
			{
				AuthenticationModel authenticationModel = JsonConvert.DeserializeObject<AuthenticationModel>(value);
				AuthenticationToken = authenticationModel.AuthenticationToken;
				IgUser = authenticationModel.IgUser;
				Hospital = authenticationModel.Hospital;
				Hospitals = authenticationModel.Hospitals ?? ((authenticationModel.Hospital != null) ? new List<HospitalModel> { authenticationModel.Hospital } : null);
				SharerLogin = authenticationModel.SharerLogin;
				SharerHospital = authenticationModel.SharerHospital;
				IsExistingUser = IgUser != null;
			}
		}
		catch (Exception)
		{
		}
		return IsExistingUser;
	}

	public async Task<bool> SaveUserModel()
	{
		bool bRes = false;
		try
		{
			bRes = await IsolatedStorageUtils.SaveAsync(JsonConvert.SerializeObject(new AuthenticationModel
			{
				AuthenticationToken = AuthenticationToken,
				IgUser = IgUser,
				Hospital = Hospital,
				Hospitals = Hospitals,
				SharerLogin = SharerLogin,
				SharerHospital = SharerHospital
			}), "login.txt");
		}
		catch (Exception)
		{
		}
		return bRes;
	}

	public async Task ResetUser()
	{
		AuthenticationToken = null;
		IgUser = null;
		Hospital = null;
		Hospitals = null;
		SharerLogin = null;
		SharerHospital = null;
		await IsolatedStorageUtils.SaveAsync(string.Empty, "login.txt");
	}
}
