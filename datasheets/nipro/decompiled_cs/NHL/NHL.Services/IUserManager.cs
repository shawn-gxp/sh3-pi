using System.Collections.Generic;
using System.Threading.Tasks;
using NHL.Models;

namespace NHL.Services;

public interface IUserManager
{
	IAuthenticationService AuthenticationService { get; set; }

	string AuthenticationToken { get; set; }

	HospitalModel Hospital { get; set; }

	IEnumerable<HospitalModel> Hospitals { get; set; }

	IgUserModel IgUser { get; set; }

	LoginModel SharerLogin { get; set; }

	HospitalModel SharerHospital { get; set; }

	IIgUserService IgUserService { get; set; }

	bool IsExistingUser { get; set; }

	Task<bool> Authenticate();

	Task Authenticate(string token);

	void Initialize(IAuthenticatedUserService authenticatedUserService, IAuthenticationService authenticationService, IHospitalInfoService hospitalInfoService, IIgUserService igUserService, ILinkHospitalService linkHospitalService, IUnlinkHospitalService unlinkHospitalService, IUpdateTimezoneService updateTimezoneService, IUpdateIgUserService updateIgUserService, IUpdateIgPasswordService updateIgPasswordService, ILogfileTransferService logfileTransferService, IUpdateIgGlucoseTargetService updateIgGlucoseTargetService, ISharerRegisterService sharerRegisterService, ISharerUpdateService sharerUpdateService, IUpdateTimezoneOtherService updateTimezoneOtherService, IVersionService versionService);

	Task<bool> RegistUser(IgUserRequestModel igUserRequestModel);

	Task<bool> RestoreUser(string backupCode, string backupPassword);

	Task<bool> LoadUserModel();

	Task<bool> SaveUserModel();

	Task ResetUser();
}
