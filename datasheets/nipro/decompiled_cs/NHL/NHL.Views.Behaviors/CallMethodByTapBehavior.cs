using NHL.Views.Behaviors.Support;
using Xamarin.Forms;

namespace NHL.Views.Behaviors;

public class CallMethodByTapBehavior : CallMethodBehaviorBase<View>
{
	private TapGestureRecognizer tapGesture;

	protected override void AddGesture(View associatedObject)
	{
		tapGesture = new TapGestureRecognizer();
		tapGesture.Command = new Command(CallMethod);
		associatedObject.GestureRecognizers.Add(tapGesture);
	}

	protected override void RemoveGesture(View associatedObject)
	{
		if (tapGesture != null)
		{
			associatedObject.GestureRecognizers.Remove(tapGesture);
			tapGesture = null;
		}
	}
}
