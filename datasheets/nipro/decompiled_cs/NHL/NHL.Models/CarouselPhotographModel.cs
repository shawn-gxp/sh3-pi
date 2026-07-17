using NHL.Models.Support;

namespace NHL.Models;

public class CarouselPhotographModel : ModelBase
{
	private Photograph leftPhoto;

	private Photograph rightPhoto;

	private bool isVisibleBack;

	private bool isVisibleNext;

	public Photograph LeftPhoto
	{
		get
		{
			return leftPhoto;
		}
		set
		{
			if (leftPhoto != value)
			{
				leftPhoto = value;
				NotifyOfPropertyChange(() => LeftPhoto);
			}
		}
	}

	public Photograph RightPhoto
	{
		get
		{
			return rightPhoto;
		}
		set
		{
			if (rightPhoto != value)
			{
				rightPhoto = value;
				NotifyOfPropertyChange(() => RightPhoto);
			}
		}
	}

	public bool IsVisibleBack
	{
		get
		{
			return isVisibleBack;
		}
		set
		{
			if (isVisibleBack != value)
			{
				isVisibleBack = value;
				NotifyOfPropertyChange(() => IsVisibleBack);
			}
		}
	}

	public bool IsVisibleNext
	{
		get
		{
			return isVisibleNext;
		}
		set
		{
			if (isVisibleNext != value)
			{
				isVisibleNext = value;
				NotifyOfPropertyChange(() => IsVisibleNext);
			}
		}
	}
}
