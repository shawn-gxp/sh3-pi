using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Common;
using NHL.Services.DependencyService;
using NHL.ViewModels.Support;
using PCLStorage;
using Xamarin.Forms;

namespace NHL.ViewModels;

public class LogViewModel : ViewModelBase
{
	public class FileItem
	{
		public enum ItemTypeEnum
		{
			File,
			Folder
		}

		private string _SizeString;

		public ItemTypeEnum ItemType { get; set; }

		public string Name { get; set; }

		public long Size { get; set; }

		public string SizeString
		{
			get
			{
				if (Size >= 1024 && Size < 1048576)
				{
					_SizeString = string.Format("{0:#0.#}K({1:#,0})", new object[2]
					{
						(double)Size / 1024.0,
						Size
					});
				}
				else if (Size >= 1048576 && Size < 1073741824)
				{
					_SizeString = string.Format("{0:#0.#}M({1:#,0})", new object[2]
					{
						(double)Size / 1048576.0,
						Size
					});
				}
				else if (Size >= 1073741824)
				{
					_SizeString = string.Format("{0:#0.#}G({1:#,0})", new object[2]
					{
						(double)Size / 1073741824.0,
						Size
					});
				}
				else if (Size > 0)
				{
					_SizeString = string.Format($"{Size:#,0}");
				}
				return _SizeString;
			}
		}

		public IFile File { get; set; }

		public IFolder Folder { get; set; }
	}

	private ILoggingService Log = DependencyService.Get<ILoggingService>();

	private BindableCollection<FileItem> _LogFileList;

	private IFile _SelectedLogFile;

	private BindableCollection<string> _LogTextList;

	public BindableCollection<FileItem> LogFileList
	{
		get
		{
			return _LogFileList;
		}
		set
		{
			_LogFileList = value;
			NotifyOfPropertyChange(() => LogFileList);
		}
	}

	public IFile SelectedLogFile
	{
		get
		{
			return _SelectedLogFile;
		}
		set
		{
			_SelectedLogFile = value;
			NotifyOfPropertyChange(() => SelectedLogFile);
		}
	}

	public BindableCollection<string> LogTextList
	{
		get
		{
			return _LogTextList;
		}
		set
		{
			_LogTextList = value;
			NotifyOfPropertyChange(() => LogTextList);
		}
	}

	public async void ViewLog(FileItem item)
	{
		try
		{
			if (item.File != null)
			{
				LogTextList = new BindableCollection<string>((await item.File.ReadAllTextAsync()).Split(new char[1] { '\n' }).ToList());
			}
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【LogViewModel】【ViewLog】例外発生：{ex}");
		}
	}

	public async void SendLog(FileItem item)
	{
		try
		{
			if (item.File != null)
			{
				await item.File.ReadAllTextAsync();
			}
		}
		catch (Exception ex)
		{
			Log.Error($"【IG】【LogViewModel】【SendLog】例外発生：{ex}");
		}
	}

	protected override void OnActivate()
	{
		base.OnActivate();
		Task.Run(async delegate
		{
			IList<IFile> list = await NHL.Common.Common.GetLogFiles();
			if (list != null)
			{
				LogFileList = new BindableCollection<FileItem>(list.Select((IFile x) => new FileItem
				{
					Name = x.Name,
					File = x,
					Size = NHL.Common.Common.GetFileSize(x)
				}).ToList());
			}
		}).ConfigureAwait(continueOnCapturedContext: false);
	}
}
