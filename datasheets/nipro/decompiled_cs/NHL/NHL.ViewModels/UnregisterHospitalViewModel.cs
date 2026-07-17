using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Framework;
using NHL.Models;
using NHL.Models.Types;
using NHL.Services;
using NHL.Services.DependencyService;
using NHL.ViewModels.Support;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class UnregisterHospitalViewModel : ViewModelBase
{
	public class SelectedItem : IEquatable<SelectedItem>
	{
		public string Name { get; set; }

		public bool Equals(SelectedItem other)
		{
			if (other == null)
			{
				return false;
			}
			return Name.Equals(other.Name);
		}
	}

	private ILoggingService Log = DependencyService.Get<ILoggingService>();

	private HospitalModel _hospital;

	private HospitalInfoResponseModel _hospitalInfo;

	private bool _isNetworkEnabled;

	private bool _isShareItemGlucose;

	private bool _isShareItemSphygmomanometer;

	private bool _isShareItemTemperature;

	private bool _isShareItemCompositionMeter;

	private bool _isShareItemStepMeter;

	private bool _isShareItemPhotograph;

	private bool _isShareItemComment;

	private bool _isShareItemGlucoseRequired;

	private bool _isShareItemSphygmomanometerRequired;

	private bool _isShareItemTemperatureRequired;

	private bool _isShareItemCompositionMeterRequired;

	private bool _isShareItemStepMeterRequired;

	private bool _isShareItemPhotographRequired;

	private bool _isShareItemCommentRequired;

	private string _shareItemGlucoseRequiredMark;

	private string _shareItemSphygmomanometerRequiredMark;

	private string _shareItemTemperatureRequiredMark;

	private string _shareItemCompositionMeterRequiredMark;

	private string _shareItemStepMeterRequiredMark;

	private string _shareItemPhotographRequiredMark;

	private string _shareItemCommentRequiredMark;

	private string[] _shareItemNameList;

	private bool[] _isShareItemTypeStringList;

	private bool[] _isShareItemTypeNumberList;

	private bool[] _isShareItemTypeDateList;

	private bool[] _isShareItemTypeSelectedList;

	private bool[] _isShareItemTypeSelectedMultipleList;

	private IList<SelectedItem>[] _shareItemSelectedItemsSource;

	private string[] _shareItemRequiredMarkList;

	private bool[] _isVisibleShareItemList;

	private Color[] _shareSettingItemColorList;

	private Color[] _shareItemColorList;

	private string _shareItem1EntryValue;

	private SelectedItem _shareItem1SelectedItem;

	private DateTime? _shareItem1At;

	private string _shareItem1AtValue;

	private string _shareItem2EntryValue;

	private SelectedItem _shareItem2SelectedItem;

	private DateTime? _shareItem2At;

	private string _shareItem2AtValue;

	private string _shareItem3EntryValue;

	private SelectedItem _shareItem3SelectedItem;

	private DateTime? _shareItem3At;

	private string _shareItem3AtValue;

	private string _shareItem4EntryValue;

	private SelectedItem _shareItem4SelectedItem;

	private DateTime? _shareItem4At;

	private string _shareItem4AtValue;

	private string _shareItem5EntryValue;

	private SelectedItem _shareItem5SelectedItem;

	private DateTime? _shareItem5At;

	private string _shareItem5AtValue;

	public IHospitalInfoService HospitalInfoService { get; set; }

	public ILinkHospitalService LinkHospitalService { get; set; }

	public IUnlinkHospitalService UnlinkHospitalService { get; set; }

	public HospitalModel Hospital
	{
		get
		{
			return _hospital;
		}
		set
		{
			if (_hospital != value)
			{
				_hospital = value;
				NotifyOfPropertyChange(() => _hospital);
			}
		}
	}

	public HospitalInfoResponseModel HospitalInfo
	{
		get
		{
			return _hospitalInfo;
		}
		set
		{
			if (_hospitalInfo != value)
			{
				_hospitalInfo = value;
				NotifyOfPropertyChange(() => HospitalInfo);
			}
		}
	}

	public bool IsNetworkEnabled
	{
		get
		{
			return _isNetworkEnabled;
		}
		set
		{
			if (_isNetworkEnabled != value)
			{
				_isNetworkEnabled = value;
				NotifyOfPropertyChange(() => IsNetworkEnabled);
			}
		}
	}

	public bool IsShareItemGlucose
	{
		get
		{
			return _isShareItemGlucose;
		}
		set
		{
			if (_isShareItemGlucose != value)
			{
				_isShareItemGlucose = value;
				NotifyOfPropertyChange(() => IsShareItemGlucose);
			}
		}
	}

	public bool IsShareItemSphygmomanometer
	{
		get
		{
			return _isShareItemSphygmomanometer;
		}
		set
		{
			if (_isShareItemSphygmomanometer != value)
			{
				_isShareItemSphygmomanometer = value;
				NotifyOfPropertyChange(() => IsShareItemSphygmomanometer);
			}
		}
	}

	public bool IsShareItemTemperature
	{
		get
		{
			return _isShareItemTemperature;
		}
		set
		{
			if (_isShareItemTemperature != value)
			{
				_isShareItemTemperature = value;
				NotifyOfPropertyChange(() => IsShareItemTemperature);
			}
		}
	}

	public bool IsShareItemCompositionMeter
	{
		get
		{
			return _isShareItemCompositionMeter;
		}
		set
		{
			if (_isShareItemCompositionMeter != value)
			{
				_isShareItemCompositionMeter = value;
				NotifyOfPropertyChange(() => IsShareItemCompositionMeter);
			}
		}
	}

	public bool IsShareItemStepMeter
	{
		get
		{
			return _isShareItemStepMeter;
		}
		set
		{
			if (_isShareItemStepMeter != value)
			{
				_isShareItemStepMeter = value;
				NotifyOfPropertyChange(() => IsShareItemStepMeter);
			}
		}
	}

	public bool IsShareItemPhotograph
	{
		get
		{
			return _isShareItemPhotograph;
		}
		set
		{
			if (_isShareItemPhotograph != value)
			{
				_isShareItemPhotograph = value;
				NotifyOfPropertyChange(() => IsShareItemPhotograph);
			}
		}
	}

	public bool IsShareItemComment
	{
		get
		{
			return _isShareItemComment;
		}
		set
		{
			if (_isShareItemComment != value)
			{
				_isShareItemComment = value;
				NotifyOfPropertyChange(() => IsShareItemComment);
			}
		}
	}

	public bool IsShareItemGlucoseRequired
	{
		get
		{
			return _isShareItemGlucoseRequired;
		}
		set
		{
			if (_isShareItemGlucoseRequired != value)
			{
				_isShareItemGlucoseRequired = value;
				NotifyOfPropertyChange(() => IsShareItemGlucoseRequired);
			}
		}
	}

	public bool IsShareItemSphygmomanometerRequired
	{
		get
		{
			return _isShareItemSphygmomanometerRequired;
		}
		set
		{
			if (_isShareItemSphygmomanometerRequired != value)
			{
				_isShareItemSphygmomanometerRequired = value;
				NotifyOfPropertyChange(() => IsShareItemSphygmomanometerRequired);
			}
		}
	}

	public bool IsShareItemTemperatureRequired
	{
		get
		{
			return _isShareItemTemperatureRequired;
		}
		set
		{
			if (_isShareItemTemperatureRequired != value)
			{
				_isShareItemTemperatureRequired = value;
				NotifyOfPropertyChange(() => IsShareItemTemperatureRequired);
			}
		}
	}

	public bool IsShareItemCompositionMeterRequired
	{
		get
		{
			return _isShareItemCompositionMeterRequired;
		}
		set
		{
			if (_isShareItemCompositionMeterRequired != value)
			{
				_isShareItemCompositionMeterRequired = value;
				NotifyOfPropertyChange(() => IsShareItemCompositionMeterRequired);
			}
		}
	}

	public bool IsShareItemStepMeterRequired
	{
		get
		{
			return _isShareItemStepMeterRequired;
		}
		set
		{
			if (_isShareItemStepMeterRequired != value)
			{
				_isShareItemStepMeterRequired = value;
				NotifyOfPropertyChange(() => IsShareItemStepMeterRequired);
			}
		}
	}

	public bool IsShareItemPhotographRequired
	{
		get
		{
			return _isShareItemPhotographRequired;
		}
		set
		{
			if (_isShareItemPhotographRequired != value)
			{
				_isShareItemPhotographRequired = value;
				NotifyOfPropertyChange(() => IsShareItemPhotographRequired);
			}
		}
	}

	public bool IsShareItemCommentRequired
	{
		get
		{
			return _isShareItemCommentRequired;
		}
		set
		{
			if (_isShareItemCommentRequired != value)
			{
				_isShareItemCommentRequired = value;
				NotifyOfPropertyChange(() => IsShareItemCommentRequired);
			}
		}
	}

	public string ShareItemGlucoseRequiredMark
	{
		get
		{
			return _shareItemGlucoseRequiredMark;
		}
		set
		{
			if (!(_shareItemGlucoseRequiredMark == value))
			{
				_shareItemGlucoseRequiredMark = value;
				NotifyOfPropertyChange(() => ShareItemGlucoseRequiredMark);
			}
		}
	}

	public string ShareItemSphygmomanometerRequiredMark
	{
		get
		{
			return _shareItemSphygmomanometerRequiredMark;
		}
		set
		{
			if (!(_shareItemSphygmomanometerRequiredMark == value))
			{
				_shareItemSphygmomanometerRequiredMark = value;
				NotifyOfPropertyChange(() => ShareItemSphygmomanometerRequiredMark);
			}
		}
	}

	public string ShareItemTemperatureRequiredMark
	{
		get
		{
			return _shareItemTemperatureRequiredMark;
		}
		set
		{
			if (!(_shareItemTemperatureRequiredMark == value))
			{
				_shareItemTemperatureRequiredMark = value;
				NotifyOfPropertyChange(() => ShareItemTemperatureRequiredMark);
			}
		}
	}

	public string ShareItemCompositionMeterRequiredMark
	{
		get
		{
			return _shareItemCompositionMeterRequiredMark;
		}
		set
		{
			if (!(_shareItemCompositionMeterRequiredMark == value))
			{
				_shareItemCompositionMeterRequiredMark = value;
				NotifyOfPropertyChange(() => ShareItemCompositionMeterRequiredMark);
			}
		}
	}

	public string ShareItemStepMeterRequiredMark
	{
		get
		{
			return _shareItemStepMeterRequiredMark;
		}
		set
		{
			if (!(_shareItemStepMeterRequiredMark == value))
			{
				_shareItemStepMeterRequiredMark = value;
				NotifyOfPropertyChange(() => ShareItemStepMeterRequiredMark);
			}
		}
	}

	public string ShareItemPhotographRequiredMark
	{
		get
		{
			return _shareItemPhotographRequiredMark;
		}
		set
		{
			if (!(_shareItemPhotographRequiredMark == value))
			{
				_shareItemPhotographRequiredMark = value;
				NotifyOfPropertyChange(() => ShareItemPhotographRequiredMark);
			}
		}
	}

	public string ShareItemCommentRequiredMark
	{
		get
		{
			return _shareItemCommentRequiredMark;
		}
		set
		{
			if (!(_shareItemCommentRequiredMark == value))
			{
				_shareItemCommentRequiredMark = value;
				NotifyOfPropertyChange(() => ShareItemCommentRequiredMark);
			}
		}
	}

	public string[] ShareItemNameList
	{
		get
		{
			return _shareItemNameList;
		}
		set
		{
			if (_shareItemNameList != value)
			{
				_shareItemNameList = value;
				NotifyOfPropertyChange(() => ShareItemNameList);
			}
		}
	}

	public bool[] IsShareItemTypeStringList
	{
		get
		{
			return _isShareItemTypeStringList;
		}
		set
		{
			if (_isShareItemTypeStringList != value)
			{
				_isShareItemTypeStringList = value;
				NotifyOfPropertyChange(() => IsShareItemTypeStringList);
			}
		}
	}

	public bool[] IsShareItemTypeNumberList
	{
		get
		{
			return _isShareItemTypeNumberList;
		}
		set
		{
			if (_isShareItemTypeNumberList != value)
			{
				_isShareItemTypeNumberList = value;
				NotifyOfPropertyChange(() => IsShareItemTypeNumberList);
			}
		}
	}

	public bool[] IsShareItemTypeDateList
	{
		get
		{
			return _isShareItemTypeDateList;
		}
		set
		{
			if (_isShareItemTypeDateList != value)
			{
				_isShareItemTypeDateList = value;
				NotifyOfPropertyChange(() => IsShareItemTypeDateList);
			}
		}
	}

	public bool[] IsShareItemTypeSelectedList
	{
		get
		{
			return _isShareItemTypeSelectedList;
		}
		set
		{
			if (_isShareItemTypeSelectedList != value)
			{
				_isShareItemTypeSelectedList = value;
				NotifyOfPropertyChange(() => IsShareItemTypeSelectedList);
			}
		}
	}

	public bool[] IsShareItemTypeSelectedMultipleList
	{
		get
		{
			return _isShareItemTypeSelectedMultipleList;
		}
		set
		{
			if (_isShareItemTypeSelectedMultipleList != value)
			{
				_isShareItemTypeSelectedMultipleList = value;
				NotifyOfPropertyChange(() => IsShareItemTypeSelectedMultipleList);
			}
		}
	}

	public IList<SelectedItem>[] ShareItemSelectedItemsSource
	{
		get
		{
			return _shareItemSelectedItemsSource;
		}
		set
		{
			_shareItemSelectedItemsSource = value;
			NotifyOfPropertyChange(() => ShareItemSelectedItemsSource);
		}
	}

	public string[] ShareItemRequiredMarkList
	{
		get
		{
			return _shareItemRequiredMarkList;
		}
		set
		{
			if (_shareItemRequiredMarkList != value)
			{
				_shareItemRequiredMarkList = value;
				NotifyOfPropertyChange(() => ShareItemRequiredMarkList);
			}
		}
	}

	public bool[] IsVisibleShareItemList
	{
		get
		{
			return _isVisibleShareItemList;
		}
		set
		{
			if (_isVisibleShareItemList != value)
			{
				_isVisibleShareItemList = value;
				NotifyOfPropertyChange(() => IsVisibleShareItemList);
			}
		}
	}

	public Color[] ShareSettingItemColorList
	{
		get
		{
			return _shareSettingItemColorList;
		}
		set
		{
			if (_shareSettingItemColorList != value)
			{
				_shareSettingItemColorList = value;
				NotifyOfPropertyChange(() => ShareSettingItemColorList);
			}
		}
	}

	public Color[] ShareItemColorList
	{
		get
		{
			return _shareItemColorList;
		}
		set
		{
			if (_shareItemColorList != value)
			{
				_shareItemColorList = value;
				NotifyOfPropertyChange(() => ShareItemColorList);
			}
		}
	}

	public string ShareItem1Name => (HospitalInfo?.HospitalShareSettings.FirstOrDefault((HospitalShareSettingModel x) => x.ItemNo == 1))?.ItemName;

	public Color ShareItem1Color { get; set; }

	public string ShareItem1RequiredMark
	{
		get
		{
			HospitalShareSettingModel hospitalShareSettingModel = HospitalInfo?.HospitalShareSettings.FirstOrDefault((HospitalShareSettingModel x) => x.ItemNo == 1);
			if (hospitalShareSettingModel == null)
			{
				return null;
			}
			if (!hospitalShareSettingModel.RequiredFlg)
			{
				return string.Empty;
			}
			return " *必須";
		}
	}

	public string ShareItem1EntryValue
	{
		get
		{
			return _shareItem1EntryValue;
		}
		set
		{
			if (!(_shareItem1EntryValue == value))
			{
				_shareItem1EntryValue = value;
				NotifyOfPropertyChange(() => ShareItem1EntryValue);
			}
		}
	}

	public IList<SelectedItem> ShareItem1SelectedItems
	{
		get
		{
			IList<SelectedItem>[] shareItemSelectedItemsSource = ShareItemSelectedItemsSource;
			if (shareItemSelectedItemsSource == null)
			{
				return null;
			}
			return shareItemSelectedItemsSource[0];
		}
	}

	public SelectedItem ShareItem1SelectedItem
	{
		get
		{
			return _shareItem1SelectedItem;
		}
		set
		{
			if (value != null && _shareItem1SelectedItem != value)
			{
				_shareItem1SelectedItem = value;
				NotifyOfPropertyChange(() => ShareItem1SelectedItem);
			}
		}
	}

	public DateTime? ShareItem1At
	{
		get
		{
			return _shareItem1At;
		}
		set
		{
			if (!(_shareItem1At == value))
			{
				_shareItem1At = value;
				NotifyOfPropertyChange(() => ShareItem1At);
				ShareItem1AtValue = ShareItem1At?.ToString();
			}
		}
	}

	public string ShareItem1AtValue
	{
		get
		{
			return _shareItem1AtValue;
		}
		set
		{
			if (!(_shareItem1AtValue == value))
			{
				_shareItem1AtValue = value;
				NotifyOfPropertyChange(() => ShareItem1AtValue);
			}
		}
	}

	public string ShareItem2Name => (HospitalInfo?.HospitalShareSettings.FirstOrDefault((HospitalShareSettingModel x) => x.ItemNo == 2))?.ItemName;

	public Color ShareItem2Color { get; set; }

	public string ShareItem2RequiredMark
	{
		get
		{
			HospitalShareSettingModel hospitalShareSettingModel = HospitalInfo?.HospitalShareSettings.FirstOrDefault((HospitalShareSettingModel x) => x.ItemNo == 2);
			if (hospitalShareSettingModel == null)
			{
				return null;
			}
			if (!hospitalShareSettingModel.RequiredFlg)
			{
				return string.Empty;
			}
			return " *必須";
		}
	}

	public string ShareItem2EntryValue
	{
		get
		{
			return _shareItem2EntryValue;
		}
		set
		{
			if (!(_shareItem2EntryValue == value))
			{
				_shareItem2EntryValue = value;
				NotifyOfPropertyChange(() => ShareItem2EntryValue);
			}
		}
	}

	public IList<SelectedItem> ShareItem2SelectedItems
	{
		get
		{
			IList<SelectedItem>[] shareItemSelectedItemsSource = ShareItemSelectedItemsSource;
			if (shareItemSelectedItemsSource == null)
			{
				return null;
			}
			return shareItemSelectedItemsSource[1];
		}
	}

	public SelectedItem ShareItem2SelectedItem
	{
		get
		{
			return _shareItem2SelectedItem;
		}
		set
		{
			if (value != null && _shareItem2SelectedItem != value)
			{
				_shareItem2SelectedItem = value;
				NotifyOfPropertyChange(() => ShareItem2SelectedItem);
			}
		}
	}

	public DateTime? ShareItem2At
	{
		get
		{
			return _shareItem2At;
		}
		set
		{
			if (!(_shareItem2At == value))
			{
				_shareItem2At = value;
				NotifyOfPropertyChange(() => ShareItem2At);
				ShareItem2AtValue = ShareItem2At?.ToString();
			}
		}
	}

	public string ShareItem2AtValue
	{
		get
		{
			return _shareItem2AtValue;
		}
		set
		{
			if (!(_shareItem2AtValue == value))
			{
				_shareItem2AtValue = value;
				NotifyOfPropertyChange(() => ShareItem2AtValue);
			}
		}
	}

	public string ShareItem3Name => (HospitalInfo?.HospitalShareSettings.FirstOrDefault((HospitalShareSettingModel x) => x.ItemNo == 3))?.ItemName;

	public Color ShareItem3Color { get; set; }

	public string ShareItem3RequiredMark
	{
		get
		{
			HospitalShareSettingModel hospitalShareSettingModel = HospitalInfo?.HospitalShareSettings.FirstOrDefault((HospitalShareSettingModel x) => x.ItemNo == 3);
			if (hospitalShareSettingModel == null)
			{
				return null;
			}
			if (!hospitalShareSettingModel.RequiredFlg)
			{
				return string.Empty;
			}
			return " *必須";
		}
	}

	public string ShareItem3EntryValue
	{
		get
		{
			return _shareItem3EntryValue;
		}
		set
		{
			if (!(_shareItem3EntryValue == value))
			{
				_shareItem3EntryValue = value;
				NotifyOfPropertyChange(() => ShareItem3EntryValue);
			}
		}
	}

	public IList<SelectedItem> ShareItem3SelectedItems
	{
		get
		{
			IList<SelectedItem>[] shareItemSelectedItemsSource = ShareItemSelectedItemsSource;
			if (shareItemSelectedItemsSource == null)
			{
				return null;
			}
			return shareItemSelectedItemsSource[2];
		}
	}

	public SelectedItem ShareItem3SelectedItem
	{
		get
		{
			return _shareItem3SelectedItem;
		}
		set
		{
			if (value != null && _shareItem3SelectedItem != value)
			{
				_shareItem3SelectedItem = value;
				NotifyOfPropertyChange(() => ShareItem3SelectedItem);
			}
		}
	}

	public DateTime? ShareItem3At
	{
		get
		{
			return _shareItem3At;
		}
		set
		{
			if (!(_shareItem3At == value))
			{
				_shareItem3At = value;
				NotifyOfPropertyChange(() => ShareItem3At);
				ShareItem3AtValue = ShareItem3At?.ToString();
			}
		}
	}

	public string ShareItem3AtValue
	{
		get
		{
			return _shareItem3AtValue;
		}
		set
		{
			if (!(_shareItem3AtValue == value))
			{
				_shareItem3AtValue = value;
				NotifyOfPropertyChange(() => ShareItem3AtValue);
			}
		}
	}

	public string ShareItem4Name => (HospitalInfo?.HospitalShareSettings.FirstOrDefault((HospitalShareSettingModel x) => x.ItemNo == 4))?.ItemName;

	public Color ShareItem4Color { get; set; }

	public string ShareItem4RequiredMark
	{
		get
		{
			HospitalShareSettingModel hospitalShareSettingModel = HospitalInfo?.HospitalShareSettings.FirstOrDefault((HospitalShareSettingModel x) => x.ItemNo == 4);
			if (hospitalShareSettingModel == null)
			{
				return null;
			}
			if (!hospitalShareSettingModel.RequiredFlg)
			{
				return string.Empty;
			}
			return " *必須";
		}
	}

	public string ShareItem4EntryValue
	{
		get
		{
			return _shareItem4EntryValue;
		}
		set
		{
			if (!(_shareItem4EntryValue == value))
			{
				_shareItem4EntryValue = value;
				NotifyOfPropertyChange(() => ShareItem4EntryValue);
			}
		}
	}

	public IList<SelectedItem> ShareItem4SelectedItems
	{
		get
		{
			IList<SelectedItem>[] shareItemSelectedItemsSource = ShareItemSelectedItemsSource;
			if (shareItemSelectedItemsSource == null)
			{
				return null;
			}
			return shareItemSelectedItemsSource[3];
		}
	}

	public SelectedItem ShareItem4SelectedItem
	{
		get
		{
			return _shareItem4SelectedItem;
		}
		set
		{
			if (value != null && _shareItem4SelectedItem != value)
			{
				_shareItem4SelectedItem = value;
				NotifyOfPropertyChange(() => ShareItem4SelectedItem);
			}
		}
	}

	public DateTime? ShareItem4At
	{
		get
		{
			return _shareItem4At;
		}
		set
		{
			if (!(_shareItem4At == value))
			{
				_shareItem4At = value;
				NotifyOfPropertyChange(() => ShareItem4At);
				ShareItem4AtValue = ShareItem4At?.ToString();
			}
		}
	}

	public string ShareItem4AtValue
	{
		get
		{
			return _shareItem4AtValue;
		}
		set
		{
			if (!(_shareItem4AtValue == value))
			{
				_shareItem4AtValue = value;
				NotifyOfPropertyChange(() => ShareItem4AtValue);
			}
		}
	}

	public string ShareItem5Name => (HospitalInfo?.HospitalShareSettings.FirstOrDefault((HospitalShareSettingModel x) => x.ItemNo == 5))?.ItemName;

	public Color ShareItem5Color { get; set; }

	public string ShareItem5RequiredMark
	{
		get
		{
			HospitalShareSettingModel hospitalShareSettingModel = HospitalInfo?.HospitalShareSettings.FirstOrDefault((HospitalShareSettingModel x) => x.ItemNo == 5);
			if (hospitalShareSettingModel == null)
			{
				return null;
			}
			if (!hospitalShareSettingModel.RequiredFlg)
			{
				return string.Empty;
			}
			return " *必須";
		}
	}

	public string ShareItem5EntryValue
	{
		get
		{
			return _shareItem5EntryValue;
		}
		set
		{
			if (!(_shareItem5EntryValue == value))
			{
				_shareItem5EntryValue = value;
				NotifyOfPropertyChange(() => ShareItem5EntryValue);
			}
		}
	}

	public IList<SelectedItem> ShareItem5SelectedItems
	{
		get
		{
			IList<SelectedItem>[] shareItemSelectedItemsSource = ShareItemSelectedItemsSource;
			if (shareItemSelectedItemsSource == null)
			{
				return null;
			}
			return shareItemSelectedItemsSource[4];
		}
	}

	public SelectedItem ShareItem5SelectedItem
	{
		get
		{
			return _shareItem5SelectedItem;
		}
		set
		{
			if (value != null && _shareItem5SelectedItem != value)
			{
				_shareItem5SelectedItem = value;
				NotifyOfPropertyChange(() => ShareItem5SelectedItem);
			}
		}
	}

	public DateTime? ShareItem5At
	{
		get
		{
			return _shareItem5At;
		}
		set
		{
			if (!(_shareItem5At == value))
			{
				_shareItem5At = value;
				NotifyOfPropertyChange(() => ShareItem5At);
				ShareItem5AtValue = ShareItem5At?.ToString();
			}
		}
	}

	public string ShareItem5AtValue
	{
		get
		{
			return _shareItem5AtValue;
		}
		set
		{
			if (!(_shareItem5AtValue == value))
			{
				_shareItem5AtValue = value;
				NotifyOfPropertyChange(() => ShareItem5AtValue);
			}
		}
	}

	public bool IsVisibleShareItem
	{
		get
		{
			if (HospitalInfo?.HospitalShareSettings != null)
			{
				return HospitalInfo.HospitalShareSettings.Any((HospitalShareSettingModel x) => x.ItemNo == 0);
			}
			return false;
		}
	}

	public bool IsVisibleShareInputItem
	{
		get
		{
			if (IsVisibleShareItemList != null)
			{
				return IsVisibleShareItemList.Any((bool x) => x);
			}
			return false;
		}
	}

	public bool IsVisibleShareItem1
	{
		get
		{
			if (IsVisibleShareItemList != null)
			{
				return IsVisibleShareItemList[0];
			}
			return false;
		}
	}

	public bool IsShareItem1TypeString
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[0] && IsShareItemTypeStringList != null)
			{
				return IsShareItemTypeStringList[0];
			}
			return false;
		}
	}

	public bool IsShareItem1TypeNumber
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[0] && IsShareItemTypeNumberList != null)
			{
				return IsShareItemTypeNumberList[0];
			}
			return false;
		}
	}

	public bool IsShareItem1TypeDate
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[0] && IsShareItemTypeDateList != null)
			{
				return IsShareItemTypeDateList[0];
			}
			return false;
		}
	}

	public bool IsShareItem1TypeSelected
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[0] && IsShareItemTypeSelectedList != null)
			{
				return IsShareItemTypeSelectedList[0];
			}
			return false;
		}
	}

	public bool IsShareItem1TypeSelectedMultiple
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[0] && IsShareItemTypeSelectedMultipleList != null)
			{
				return IsShareItemTypeSelectedMultipleList[0];
			}
			return false;
		}
	}

	public bool IsVisibleShareItem2
	{
		get
		{
			if (IsVisibleShareItemList != null)
			{
				return IsVisibleShareItemList[1];
			}
			return false;
		}
	}

	public bool IsShareItem2TypeString
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[1] && IsShareItemTypeStringList != null)
			{
				return IsShareItemTypeStringList[1];
			}
			return false;
		}
	}

	public bool IsShareItem2TypeNumber
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[1] && IsShareItemTypeNumberList != null)
			{
				return IsShareItemTypeNumberList[1];
			}
			return false;
		}
	}

	public bool IsShareItem2TypeDate
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[1] && IsShareItemTypeDateList != null)
			{
				return IsShareItemTypeDateList[1];
			}
			return false;
		}
	}

	public bool IsShareItem2TypeSelected
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[1] && IsShareItemTypeSelectedList != null)
			{
				return IsShareItemTypeSelectedList[1];
			}
			return false;
		}
	}

	public bool IsShareItem2TypeSelectedMultiple
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[1] && IsShareItemTypeSelectedMultipleList != null)
			{
				return IsShareItemTypeSelectedMultipleList[1];
			}
			return false;
		}
	}

	public bool IsVisibleShareItem3
	{
		get
		{
			if (IsVisibleShareItemList != null)
			{
				return IsVisibleShareItemList[2];
			}
			return false;
		}
	}

	public bool IsShareItem3TypeString
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[2] && IsShareItemTypeStringList != null)
			{
				return IsShareItemTypeStringList[2];
			}
			return false;
		}
	}

	public bool IsShareItem3TypeNumber
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[2] && IsShareItemTypeNumberList != null)
			{
				return IsShareItemTypeNumberList[2];
			}
			return false;
		}
	}

	public bool IsShareItem3TypeDate
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[2] && IsShareItemTypeDateList != null)
			{
				return IsShareItemTypeDateList[2];
			}
			return false;
		}
	}

	public bool IsShareItem3TypeSelected
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[2] && IsShareItemTypeSelectedList != null)
			{
				return IsShareItemTypeSelectedList[2];
			}
			return false;
		}
	}

	public bool IsShareItem3TypeSelectedMultiple
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[2] && IsShareItemTypeSelectedMultipleList != null)
			{
				return IsShareItemTypeSelectedMultipleList[2];
			}
			return false;
		}
	}

	public bool IsVisibleShareItem4
	{
		get
		{
			if (IsVisibleShareItemList != null)
			{
				return IsVisibleShareItemList[3];
			}
			return false;
		}
	}

	public bool IsShareItem4TypeString
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[3] && IsShareItemTypeStringList != null)
			{
				return IsShareItemTypeStringList[3];
			}
			return false;
		}
	}

	public bool IsShareItem4TypeNumber
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[3] && IsShareItemTypeNumberList != null)
			{
				return IsShareItemTypeNumberList[3];
			}
			return false;
		}
	}

	public bool IsShareItem4TypeDate
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[3] && IsShareItemTypeDateList != null)
			{
				return IsShareItemTypeDateList[3];
			}
			return false;
		}
	}

	public bool IsShareItem4TypeSelected
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[3] && IsShareItemTypeSelectedList != null)
			{
				return IsShareItemTypeSelectedList[3];
			}
			return false;
		}
	}

	public bool IsShareItem4TypeSelectedMultiple
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[3] && IsShareItemTypeSelectedMultipleList != null)
			{
				return IsShareItemTypeSelectedMultipleList[3];
			}
			return false;
		}
	}

	public bool IsVisibleShareItem5
	{
		get
		{
			if (IsVisibleShareItemList != null)
			{
				return IsVisibleShareItemList[4];
			}
			return false;
		}
	}

	public bool IsShareItem5TypeString
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[4] && IsShareItemTypeStringList != null)
			{
				return IsShareItemTypeStringList[4];
			}
			return false;
		}
	}

	public bool IsShareItem5TypeNumber
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[4] && IsShareItemTypeNumberList != null)
			{
				return IsShareItemTypeNumberList[4];
			}
			return false;
		}
	}

	public bool IsShareItem5TypeDate
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[4] && IsShareItemTypeDateList != null)
			{
				return IsShareItemTypeDateList[4];
			}
			return false;
		}
	}

	public bool IsShareItem5TypeSelected
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[4] && IsShareItemTypeSelectedList != null)
			{
				return IsShareItemTypeSelectedList[4];
			}
			return false;
		}
	}

	public bool IsShareItem5TypeSelectedMultiple
	{
		get
		{
			if (IsVisibleShareItemList != null && IsVisibleShareItemList[4] && IsShareItemTypeSelectedMultipleList != null)
			{
				return IsShareItemTypeSelectedMultipleList[4];
			}
			return false;
		}
	}

	protected override async void OnActivate()
	{
		Initialize();
		await ExecAsync(async delegate
		{
			try
			{
				IsNetworkEnabled = false;
				string id = Convert.ToBase64String(Encoding.UTF8.GetBytes(Hospital.Id));
				HospitalInfo = await HospitalInfoService.GetHospitalInfo(id);
				string text = HospitalInfo.HospitalShareSettings.FirstOrDefault((HospitalShareSettingModel x) => x.ItemNo == 0)?.SelectItems ?? string.Empty;
				IsShareItemGlucoseRequired = !text.Contains("glucose");
				IsShareItemSphygmomanometerRequired = !text.Contains("sphygmomanometer");
				IsShareItemTemperatureRequired = !text.Contains("temperature");
				IsShareItemCompositionMeterRequired = !text.Contains("compositionMeter");
				IsShareItemStepMeterRequired = !text.Contains("stepMeter");
				IsShareItemPhotographRequired = !text.Contains("photograph");
				IsShareItemCommentRequired = !text.Contains("comment");
				ShareItemGlucoseRequiredMark = (text.Contains("glucose") ? " *必須" : string.Empty);
				ShareItemSphygmomanometerRequiredMark = (text.Contains("sphygmomanometer") ? " *必須" : string.Empty);
				ShareItemTemperatureRequiredMark = (text.Contains("temperature") ? " *必須" : string.Empty);
				ShareItemCompositionMeterRequiredMark = (text.Contains("compositionMeter") ? " *必須" : string.Empty);
				ShareItemStepMeterRequiredMark = (text.Contains("stepMeter") ? " *必須" : string.Empty);
				ShareItemPhotographRequiredMark = (text.Contains("photograph") ? " *必須" : string.Empty);
				ShareItemCommentRequiredMark = (text.Contains("comment") ? " *必須" : string.Empty);
				ShareItem1At = InitializeShareItemAt(1);
				ShareItem2At = InitializeShareItemAt(2);
				ShareItem3At = InitializeShareItemAt(3);
				ShareItem4At = InitializeShareItemAt(4);
				ShareItem5At = InitializeShareItemAt(5);
				Execute.BeginOnUIThread(async delegate
				{
					await Task.Delay(10);
					HospitalShareSettingModel settings = HospitalInfo.HospitalShareSettings.FirstOrDefault((HospitalShareSettingModel x) => x.ItemNo == 0);
					IgUserHospitalShareItemModel igUserHospitalShareItemModel = HospitalInfo.IgUserHospitalShareItems.FirstOrDefault((IgUserHospitalShareItemModel x) => x.ItemId == settings?.Id);
					bool valueOrDefault = settings?.SelectItems?.Contains("glucose") == true;
					bool valueOrDefault2 = settings?.SelectItems?.Contains("sphygmomanometer") == true;
					bool valueOrDefault3 = settings?.SelectItems?.Contains("temperature") == true;
					bool valueOrDefault4 = settings?.SelectItems?.Contains("compositionMeter") == true;
					bool valueOrDefault5 = settings?.SelectItems?.Contains("stepMeter") == true;
					bool valueOrDefault6 = settings?.SelectItems?.Contains("photograph") == true;
					bool valueOrDefault7 = settings?.SelectItems?.Contains("comment") == true;
					bool flag = igUserHospitalShareItemModel?.Value.Contains("glucose") ?? true;
					bool flag2 = igUserHospitalShareItemModel?.Value.Contains("sphygmomanometer") ?? true;
					bool flag3 = igUserHospitalShareItemModel?.Value.Contains("temperature") ?? true;
					bool flag4 = igUserHospitalShareItemModel?.Value.Contains("compositionMeter") ?? true;
					bool flag5 = igUserHospitalShareItemModel?.Value.Contains("stepMeter") ?? true;
					bool flag6 = igUserHospitalShareItemModel?.Value.Contains("photograph") ?? true;
					bool flag7 = igUserHospitalShareItemModel?.Value.Contains("comment") ?? true;
					IsShareItemGlucose = valueOrDefault || flag;
					IsShareItemSphygmomanometer = valueOrDefault2 || flag2;
					IsShareItemTemperature = valueOrDefault3 || flag3;
					IsShareItemCompositionMeter = valueOrDefault4 || flag4;
					IsShareItemStepMeter = valueOrDefault5 || flag5;
					IsShareItemPhotograph = valueOrDefault6 || flag6;
					IsShareItemComment = valueOrDefault7 || flag7;
					ShareSettingItemColorList = new Color[7]
					{
						(valueOrDefault && !flag) ? Color.Red : Color.Black,
						(valueOrDefault2 && !flag2) ? Color.Red : Color.Black,
						(valueOrDefault3 && !flag3) ? Color.Red : Color.Black,
						(valueOrDefault4 && !flag4) ? Color.Red : Color.Black,
						(valueOrDefault5 && !flag5) ? Color.Red : Color.Black,
						(valueOrDefault6 && !flag6) ? Color.Red : Color.Black,
						(valueOrDefault7 && !flag7) ? Color.Red : Color.Black
					};
					for (int num = 0; num < 5; num++)
					{
						SetShareItemList(num);
					}
					NotifyProperties();
				});
				IsNetworkEnabled = true;
			}
			catch (Exception ex)
			{
				Log.Error($"【IG】【UnregisterHospitalViewModel】【OnActivate】例外発生：{ex}");
				await Execute.OnUIThreadAsync(async delegate
				{
					await base.DialogProvider.ShowError("", "インターネット接続を確認して、再度試してください", "OK", null);
				});
			}
		});
		base.OnActivate();
	}

	protected override void OnDeactivate(bool close)
	{
		base.OnDeactivate(close);
		HospitalInfo = null;
		ShareItem1EntryValue = string.Empty;
		ShareItem1At = null;
		ShareItem1Color = Color.Black;
		ShareItem2EntryValue = string.Empty;
		ShareItem2At = null;
		ShareItem2Color = Color.Black;
		ShareItem3EntryValue = string.Empty;
		ShareItem3At = null;
		ShareItem3Color = Color.Black;
		ShareItem4EntryValue = string.Empty;
		ShareItem4At = null;
		ShareItem4Color = Color.Black;
		ShareItem5EntryValue = string.Empty;
		ShareItem5At = null;
		ShareItem5Color = Color.Black;
		_shareItem1SelectedItem = null;
		_shareItem2SelectedItem = null;
		_shareItem3SelectedItem = null;
		_shareItem4SelectedItem = null;
		_shareItem5SelectedItem = null;
	}

	private void Initialize()
	{
		IsShareItemGlucose = true;
		IsShareItemSphygmomanometer = true;
		IsShareItemTemperature = true;
		IsShareItemCompositionMeter = true;
		IsShareItemStepMeter = true;
		IsShareItemPhotograph = true;
		IsShareItemComment = true;
		IsShareItemGlucoseRequired = false;
		IsShareItemSphygmomanometerRequired = false;
		IsShareItemTemperatureRequired = false;
		IsShareItemCompositionMeterRequired = false;
		IsShareItemStepMeterRequired = false;
		IsShareItemPhotographRequired = false;
		IsShareItemCommentRequired = false;
		ShareItemGlucoseRequiredMark = string.Empty;
		ShareItemSphygmomanometerRequiredMark = string.Empty;
		ShareItemTemperatureRequiredMark = string.Empty;
		ShareItemCompositionMeterRequiredMark = string.Empty;
		ShareItemStepMeterRequiredMark = string.Empty;
		ShareItemPhotographRequiredMark = string.Empty;
		ShareItemCommentRequiredMark = string.Empty;
		ShareItemNameList = null;
		IsShareItemTypeStringList = null;
		IsShareItemTypeNumberList = null;
		IsShareItemTypeDateList = null;
		IsShareItemTypeSelectedList = null;
		IsShareItemTypeSelectedMultipleList = null;
		ShareItemSelectedItemsSource = null;
		ShareItemRequiredMarkList = null;
		IsVisibleShareItemList = null;
		ShareSettingItemColorList = null;
		ShareItemColorList = null;
		NotifyProperties();
	}

	private DateTime? InitializeShareItemAt(int itemNo)
	{
		HospitalShareSettingModel setting = HospitalInfo.HospitalShareSettings.FirstOrDefault((HospitalShareSettingModel s) => s.ItemNo == itemNo);
		if (!string.IsNullOrEmpty(HospitalInfo.IgUserHospitalShareItems.FirstOrDefault((IgUserHospitalShareItemModel x) => x.ItemId == setting?.Id)?.Value))
		{
			return new DateTime(1, 1, 1, 0, 0, 0);
		}
		return null;
	}

	private void SetShareItemList(int itemNo)
	{
		if (ShareItemNameList == null || ShareItemNameList.Length == 0)
		{
			ShareItemNameList = new string[5];
		}
		if (IsShareItemTypeStringList == null || IsShareItemTypeStringList.Length == 0)
		{
			IsShareItemTypeStringList = new bool[5];
		}
		if (IsShareItemTypeNumberList == null || IsShareItemTypeNumberList.Length == 0)
		{
			IsShareItemTypeNumberList = new bool[5];
		}
		if (IsShareItemTypeDateList == null || IsShareItemTypeDateList.Length == 0)
		{
			IsShareItemTypeDateList = new bool[5];
		}
		if (IsShareItemTypeSelectedList == null || IsShareItemTypeSelectedList.Length == 0)
		{
			IsShareItemTypeSelectedList = new bool[5];
		}
		if (IsShareItemTypeSelectedMultipleList == null || IsShareItemTypeSelectedMultipleList.Length == 0)
		{
			IsShareItemTypeSelectedMultipleList = new bool[5];
		}
		if (ShareItemSelectedItemsSource == null || ShareItemSelectedItemsSource.Length == 0)
		{
			ShareItemSelectedItemsSource = new IList<SelectedItem>[5];
		}
		if (ShareItemRequiredMarkList == null || ShareItemRequiredMarkList.Length == 0)
		{
			ShareItemRequiredMarkList = new string[5];
		}
		if (IsVisibleShareItemList == null || IsVisibleShareItemList.Length == 0)
		{
			IsVisibleShareItemList = new bool[5];
		}
		if (ShareItemColorList == null || ShareItemColorList.Length == 0)
		{
			ShareItemColorList = new Color[5];
		}
		HospitalShareSettingModel setting = HospitalInfo.HospitalShareSettings.FirstOrDefault((HospitalShareSettingModel x) => x.ItemNo == itemNo + 1);
		ShareSettingType instance = ShareSettingType.GetInstance(setting?.Type);
		ShareItemNameList[itemNo] = setting?.ItemName;
		IsShareItemTypeStringList[itemNo] = instance.Equals(ShareSettingType.STRING);
		IsShareItemTypeNumberList[itemNo] = instance.Equals(ShareSettingType.NUMBER);
		IsShareItemTypeDateList[itemNo] = instance.Equals(ShareSettingType.DATE);
		IsShareItemTypeSelectedList[itemNo] = instance.Equals(ShareSettingType.SELECTED);
		IsShareItemTypeSelectedMultipleList[itemNo] = instance.Equals(ShareSettingType.SELECTED_MULTIPLE);
		ShareItemSelectedItemsSource[itemNo] = (from x in (setting?.SelectItems ?? string.Empty).Split(new char[1] { ',' })
			select new SelectedItem
			{
				Name = x
			}).ToList();
		string[] shareItemRequiredMarkList = ShareItemRequiredMarkList;
		int num = itemNo;
		HospitalShareSettingModel hospitalShareSettingModel = setting;
		shareItemRequiredMarkList[num] = ((hospitalShareSettingModel != null && hospitalShareSettingModel.RequiredFlg) ? " *必須" : string.Empty);
		IsVisibleShareItemList[itemNo] = setting != null;
		IgUserHospitalShareItemModel item = HospitalInfo.IgUserHospitalShareItems.FirstOrDefault((IgUserHospitalShareItemModel x) => x.ItemId == setting?.Id);
		if (instance.Equals(ShareSettingType.STRING) || instance.Equals(ShareSettingType.NUMBER))
		{
			if (itemNo == 0)
			{
				ShareItem1EntryValue = item?.Value;
			}
			else if (itemNo == 1)
			{
				ShareItem2EntryValue = item?.Value;
			}
			else if (itemNo == 2)
			{
				ShareItem3EntryValue = item?.Value;
			}
			else if (itemNo == 3)
			{
				ShareItem4EntryValue = item?.Value;
			}
			else if (itemNo == 4)
			{
				ShareItem5EntryValue = item?.Value;
			}
		}
		else if (instance.Equals(ShareSettingType.DATE))
		{
			if (itemNo == 0)
			{
				ShareItem1At = (string.IsNullOrEmpty(item?.Value) ? ((DateTime?)null) : new DateTime?(DateTime.Parse(item.Value)));
			}
			else if (itemNo == 1)
			{
				ShareItem2At = (string.IsNullOrEmpty(item?.Value) ? ((DateTime?)null) : new DateTime?(DateTime.Parse(item.Value)));
			}
			else if (itemNo == 2)
			{
				ShareItem3At = (string.IsNullOrEmpty(item?.Value) ? ((DateTime?)null) : new DateTime?(DateTime.Parse(item.Value)));
			}
			else if (itemNo == 3)
			{
				ShareItem4At = (string.IsNullOrEmpty(item?.Value) ? ((DateTime?)null) : new DateTime?(DateTime.Parse(item.Value)));
			}
			else if (itemNo == 4)
			{
				ShareItem5At = (string.IsNullOrEmpty(item?.Value) ? ((DateTime?)null) : new DateTime?(DateTime.Parse(item.Value)));
			}
		}
		else if (instance.Equals(ShareSettingType.SELECTED))
		{
			if (itemNo == 0)
			{
				IList<SelectedItem>[] shareItemSelectedItemsSource = ShareItemSelectedItemsSource;
				ShareItem1SelectedItem = ((shareItemSelectedItemsSource != null) ? shareItemSelectedItemsSource[itemNo].FirstOrDefault((SelectedItem x) => x.Name == item?.Value) : null);
			}
			else if (itemNo == 1)
			{
				IList<SelectedItem>[] shareItemSelectedItemsSource2 = ShareItemSelectedItemsSource;
				ShareItem2SelectedItem = ((shareItemSelectedItemsSource2 != null) ? shareItemSelectedItemsSource2[itemNo].FirstOrDefault((SelectedItem x) => x.Name == item?.Value) : null);
			}
			else if (itemNo == 2)
			{
				IList<SelectedItem>[] shareItemSelectedItemsSource3 = ShareItemSelectedItemsSource;
				ShareItem3SelectedItem = ((shareItemSelectedItemsSource3 != null) ? shareItemSelectedItemsSource3[itemNo].FirstOrDefault((SelectedItem x) => x.Name == item?.Value) : null);
			}
			else if (itemNo == 3)
			{
				IList<SelectedItem>[] shareItemSelectedItemsSource4 = ShareItemSelectedItemsSource;
				ShareItem4SelectedItem = ((shareItemSelectedItemsSource4 != null) ? shareItemSelectedItemsSource4[itemNo].FirstOrDefault((SelectedItem x) => x.Name == item?.Value) : null);
			}
			else if (itemNo == 4)
			{
				IList<SelectedItem>[] shareItemSelectedItemsSource5 = ShareItemSelectedItemsSource;
				ShareItem5SelectedItem = ((shareItemSelectedItemsSource5 != null) ? shareItemSelectedItemsSource5[itemNo].FirstOrDefault((SelectedItem x) => x.Name == item?.Value) : null);
			}
		}
		if (itemNo == 0)
		{
			ShareItem1Color = ((item == null || (setting.RequiredFlg && string.IsNullOrEmpty(item?.Value))) ? Color.Red : Color.Black);
		}
		else if (itemNo == 1)
		{
			ShareItem2Color = ((item == null || (setting.RequiredFlg && string.IsNullOrEmpty(item?.Value))) ? Color.Red : Color.Black);
		}
		else if (itemNo == 2)
		{
			ShareItem3Color = ((item == null || (setting.RequiredFlg && string.IsNullOrEmpty(item?.Value))) ? Color.Red : Color.Black);
		}
		else if (itemNo == 3)
		{
			ShareItem4Color = ((item == null || (setting.RequiredFlg && string.IsNullOrEmpty(item?.Value))) ? Color.Red : Color.Black);
		}
		else if (itemNo == 4)
		{
			ShareItem5Color = ((item == null || (setting.RequiredFlg && string.IsNullOrEmpty(item?.Value))) ? Color.Red : Color.Black);
		}
	}

	public async void Update()
	{
		List<IgUserHospitalShareItemModel> shareItemList = new List<IgUserHospitalShareItemModel>();
		HospitalShareSettingModel hospitalShareSettingModel = HospitalInfo.HospitalShareSettings.FirstOrDefault((HospitalShareSettingModel x) => x.ItemNo == 0);
		if (hospitalShareSettingModel != null)
		{
			List<string> list = new List<string>();
			if (IsShareItemGlucose)
			{
				list.Add("glucose");
			}
			if (IsShareItemSphygmomanometer)
			{
				list.Add("sphygmomanometer");
			}
			if (IsShareItemTemperature)
			{
				list.Add("temperature");
			}
			if (IsShareItemCompositionMeter)
			{
				list.Add("compositionMeter");
			}
			if (IsShareItemStepMeter)
			{
				list.Add("stepMeter");
			}
			if (IsShareItemPhotograph)
			{
				list.Add("photograph");
			}
			if (IsShareItemComment)
			{
				list.Add("comment");
			}
			shareItemList.Add(new IgUserHospitalShareItemModel
			{
				ItemId = hospitalShareSettingModel.Id,
				Value = string.Join(",", list)
			});
		}
		bool flag = !(await IsValid(1, shareItemList));
		if (!flag)
		{
			flag = !(await IsValid(2, shareItemList));
		}
		bool flag2 = flag;
		if (!flag2)
		{
			flag2 = !(await IsValid(3, shareItemList));
		}
		bool flag3 = flag2;
		if (!flag3)
		{
			flag3 = !(await IsValid(4, shareItemList));
		}
		bool flag4 = flag3;
		if (!flag4)
		{
			flag4 = !(await IsValid(5, shareItemList));
		}
		if (flag4)
		{
			return;
		}
		await ExecAsync(async delegate
		{
			try
			{
				await LinkHospitalService.PutLinkHospital(new LinkHospitalRequestModel
				{
					HospitalId = _hospitalInfo.Hospital.Id,
					IgUserHospitalShareItemDtoList = shareItemList
				});
				await base.UserManager.Authenticate();
				await base.UserManager.SaveUserModel();
			}
			catch (HttpErrorExceptionBase httpErrorExceptionBase)
			{
				Log.Error($"【IG】【UnregisterHospitalViewModel】【Update】例外発生：{httpErrorExceptionBase}");
				if (httpErrorExceptionBase.HttpStatusCode == HttpStatusCode.BadRequest)
				{
					await Execute.OnUIThreadAsync(async delegate
					{
						await base.DialogProvider.ShowError("", "共有情報が更新されているため、再読み込みしてください。", "OK", null);
					});
				}
				else
				{
					await Execute.OnUIThreadAsync(async delegate
					{
						await base.DialogProvider.ShowError("", "インターネット接続を確認して、再度試してください", "OK", null);
					});
				}
				return;
			}
			catch (Exception ex)
			{
				Log.Error($"【IG】【UnregisterHospitalViewModel】【Update】例外発生：{ex}");
				await Execute.OnUIThreadAsync(async delegate
				{
					await base.DialogProvider.ShowError("", "インターネット接続を確認して、再度試してください", "OK", null);
				});
				return;
			}
			Execute.BeginOnUIThread(async delegate
			{
				await base.NavigationService.GoBackAsync();
			});
		});
	}

	public async void UnRegister()
	{
		if (!(await base.DialogProvider.ShowAlert("【確認】", "このデータ共有先の登録を解除しますか？", "はい", "キャンセル", null)))
		{
			return;
		}
		await ExecAsync(async delegate
		{
			await UnlinkHospitalService.UnlinkHospital(Hospital.Id);
			Execute.BeginOnUIThread(async delegate
			{
				await base.NavigationService.GoBackAsync();
			});
		});
	}

	private async Task<bool> IsValid(int itemNo, ICollection<IgUserHospitalShareItemModel> list)
	{
		string text = ((itemNo == 1) ? ShareItem1EntryValue : ((itemNo == 2) ? ShareItem2EntryValue : ((itemNo == 3) ? ShareItem3EntryValue : ((itemNo == 4) ? ShareItem4EntryValue : ((itemNo == 5) ? ShareItem5EntryValue : string.Empty)))));
		string text2 = ((itemNo == 1) ? ShareItem1AtValue : ((itemNo == 2) ? ShareItem2AtValue : ((itemNo == 3) ? ShareItem3AtValue : ((itemNo == 4) ? ShareItem4AtValue : ((itemNo == 5) ? ShareItem5AtValue : null)))));
		string text3 = ((itemNo != 1) ? ((itemNo != 2) ? ((itemNo != 3) ? ((itemNo != 4) ? ((itemNo != 5) ? null : ShareItem5SelectedItem?.Name) : ShareItem4SelectedItem?.Name) : ShareItem3SelectedItem?.Name) : ShareItem2SelectedItem?.Name) : ShareItem1SelectedItem?.Name);
		HospitalShareSettingModel hospitalShareSettingModel = HospitalInfo.HospitalShareSettings.FirstOrDefault((HospitalShareSettingModel x) => x.ItemNo == itemNo);
		if (hospitalShareSettingModel == null)
		{
			return true;
		}
		if (hospitalShareSettingModel.RequiredFlg && ((ShareSettingType.GetInstance(hospitalShareSettingModel.Type).Equals(ShareSettingType.STRING) && string.IsNullOrEmpty(text)) || (ShareSettingType.GetInstance(hospitalShareSettingModel.Type).Equals(ShareSettingType.NUMBER) && string.IsNullOrEmpty(text)) || (ShareSettingType.GetInstance(hospitalShareSettingModel.Type).Equals(ShareSettingType.DATE) && text2 == null) || (ShareSettingType.GetInstance(hospitalShareSettingModel.Type).Equals(ShareSettingType.SELECTED) && string.IsNullOrEmpty(text3))))
		{
			await base.DialogProvider.ShowAlert("入力チェックエラー", "「" + hospitalShareSettingModel.ItemName + "」を入力してください");
			return false;
		}
		decimal result;
		if (ShareSettingType.GetInstance(hospitalShareSettingModel.Type).Equals(ShareSettingType.STRING) && !string.IsNullOrEmpty(text))
		{
			if (text.Length > 128)
			{
				await base.DialogProvider.ShowAlert("入力チェックエラー", "「" + hospitalShareSettingModel.ItemName + "」は128文字以内で入力してください");
				return false;
			}
		}
		else if (ShareSettingType.GetInstance(hospitalShareSettingModel.Type).Equals(ShareSettingType.NUMBER) && !string.IsNullOrEmpty(text) && !decimal.TryParse(text, out result))
		{
			await base.DialogProvider.ShowAlert("書式チェックエラー", "「" + hospitalShareSettingModel.ItemName + "」には有効な数値を入力してください");
			return false;
		}
		if (ShareSettingType.GetInstance(hospitalShareSettingModel.Type).Equals(ShareSettingType.STRING))
		{
			list.Add(new IgUserHospitalShareItemModel
			{
				ItemId = hospitalShareSettingModel.Id,
				Value = (string.IsNullOrEmpty(text) ? null : text)
			});
		}
		else if (ShareSettingType.GetInstance(hospitalShareSettingModel.Type).Equals(ShareSettingType.NUMBER))
		{
			list.Add(new IgUserHospitalShareItemModel
			{
				ItemId = hospitalShareSettingModel.Id,
				Value = (string.IsNullOrEmpty(text) ? null : decimal.Parse(text).ToString())
			});
		}
		else if (ShareSettingType.GetInstance(hospitalShareSettingModel.Type).Equals(ShareSettingType.DATE))
		{
			list.Add(new IgUserHospitalShareItemModel
			{
				ItemId = hospitalShareSettingModel.Id,
				Value = (string.IsNullOrEmpty(text2) ? null : text2)
			});
		}
		else if (ShareSettingType.GetInstance(hospitalShareSettingModel.Type).Equals(ShareSettingType.SELECTED))
		{
			list.Add(new IgUserHospitalShareItemModel
			{
				ItemId = hospitalShareSettingModel.Id,
				Value = (string.IsNullOrWhiteSpace(text3) ? null : text3)
			});
		}
		return true;
	}

	private void NotifyProperties()
	{
		NotifyOfPropertyChange(() => IsVisibleShareItem);
		NotifyOfPropertyChange(() => IsVisibleShareInputItem);
		NotifyOfPropertyChange(() => IsVisibleShareItem1);
		NotifyOfPropertyChange(() => ShareItem1Name);
		NotifyOfPropertyChange(() => ShareItem1RequiredMark);
		NotifyOfPropertyChange(() => ShareItem1Color);
		NotifyOfPropertyChange(() => ShareItem1SelectedItems);
		NotifyOfPropertyChange(() => ShareItem1SelectedItem);
		NotifyOfPropertyChange(() => IsShareItem1TypeString);
		NotifyOfPropertyChange(() => IsShareItem1TypeNumber);
		NotifyOfPropertyChange(() => IsShareItem1TypeDate);
		NotifyOfPropertyChange(() => IsShareItem1TypeSelected);
		NotifyOfPropertyChange(() => IsShareItem1TypeSelectedMultiple);
		NotifyOfPropertyChange(() => IsVisibleShareItem2);
		NotifyOfPropertyChange(() => ShareItem2Name);
		NotifyOfPropertyChange(() => ShareItem2RequiredMark);
		NotifyOfPropertyChange(() => ShareItem2Color);
		NotifyOfPropertyChange(() => ShareItem2SelectedItems);
		NotifyOfPropertyChange(() => ShareItem2SelectedItem);
		NotifyOfPropertyChange(() => IsShareItem2TypeString);
		NotifyOfPropertyChange(() => IsShareItem2TypeNumber);
		NotifyOfPropertyChange(() => IsShareItem2TypeDate);
		NotifyOfPropertyChange(() => IsShareItem2TypeSelected);
		NotifyOfPropertyChange(() => IsShareItem2TypeSelectedMultiple);
		NotifyOfPropertyChange(() => IsVisibleShareItem3);
		NotifyOfPropertyChange(() => ShareItem3Name);
		NotifyOfPropertyChange(() => ShareItem3RequiredMark);
		NotifyOfPropertyChange(() => ShareItem3Color);
		NotifyOfPropertyChange(() => ShareItem3SelectedItems);
		NotifyOfPropertyChange(() => ShareItem3SelectedItem);
		NotifyOfPropertyChange(() => IsShareItem3TypeString);
		NotifyOfPropertyChange(() => IsShareItem3TypeNumber);
		NotifyOfPropertyChange(() => IsShareItem3TypeDate);
		NotifyOfPropertyChange(() => IsShareItem3TypeSelected);
		NotifyOfPropertyChange(() => IsShareItem3TypeSelectedMultiple);
		NotifyOfPropertyChange(() => IsVisibleShareItem4);
		NotifyOfPropertyChange(() => ShareItem4Name);
		NotifyOfPropertyChange(() => ShareItem4RequiredMark);
		NotifyOfPropertyChange(() => ShareItem4Color);
		NotifyOfPropertyChange(() => ShareItem4SelectedItems);
		NotifyOfPropertyChange(() => ShareItem4SelectedItem);
		NotifyOfPropertyChange(() => IsShareItem4TypeString);
		NotifyOfPropertyChange(() => IsShareItem4TypeNumber);
		NotifyOfPropertyChange(() => IsShareItem4TypeDate);
		NotifyOfPropertyChange(() => IsShareItem4TypeSelected);
		NotifyOfPropertyChange(() => IsShareItem4TypeSelectedMultiple);
		NotifyOfPropertyChange(() => IsVisibleShareItem5);
		NotifyOfPropertyChange(() => ShareItem5Name);
		NotifyOfPropertyChange(() => ShareItem5RequiredMark);
		NotifyOfPropertyChange(() => ShareItem5Color);
		NotifyOfPropertyChange(() => ShareItem5SelectedItems);
		NotifyOfPropertyChange(() => ShareItem5SelectedItem);
		NotifyOfPropertyChange(() => IsShareItem5TypeString);
		NotifyOfPropertyChange(() => IsShareItem5TypeNumber);
		NotifyOfPropertyChange(() => IsShareItem5TypeDate);
		NotifyOfPropertyChange(() => IsShareItem5TypeSelected);
		NotifyOfPropertyChange(() => IsShareItem5TypeSelectedMultiple);
	}
}
