using System;
using System.Linq;
using Android.App;
using Android.Content;
using Android.Graphics;
using Android.Graphics.Drawables;
using Android.Views;
using Android.Widget;
using NHL.Views.Renderer;
using Xamarin.Forms;
using Xamarin.Forms.Platform.Android;

namespace NHL.Droid.Renderer;

public class TimeZoneOtherPickerRenderer : PickerRenderer
{
	private Typeface fontFace;

	private AlertDialog _dialog;

	public string[] array3 = (from x in Enumerable.Range(0, 60)
		select $"{x:00}").ToArray();

	private IElementController ElementController => base.Element;

	public TimeZoneOtherPickerRenderer(Context context)
		: base(context)
	{
		base.AutoPackage = false;
	}

	[Obsolete("This constructor is obsolete as of version 2.5. Please use PickerRenderer(Context) instead.")]
	public TimeZoneOtherPickerRenderer()
	{
		base.AutoPackage = false;
	}

	protected override void OnElementChanged(ElementChangedEventArgs<Picker> e)
	{
		base.OnElementChanged(e);
		if (e.NewElement != null && e.OldElement == null && base.Control != null)
		{
			GradientDrawable gradientDrawable = new GradientDrawable();
			gradientDrawable.SetStroke(0, Android.Graphics.Color.Transparent);
			base.Control.SetBackground(gradientDrawable);
			base.Control.TextSize = 14f;
			base.Control.Click += Control_Click;
		}
	}

	protected override void Dispose(bool disposing)
	{
		base.Control.Click -= Control_Click;
		base.Dispose(disposing);
	}

	private void Control_Click(object sender, EventArgs e)
	{
		TimeZoneOtherPicker model = base.Element as TimeZoneOtherPicker;
		NumberPicker picker = new NumberPicker(((Android.Views.View)(object)this).Context);
		NumberPicker picker2 = new NumberPicker(((Android.Views.View)(object)this).Context);
		TextView textView = new TextView(((Android.Views.View)(object)this).Context);
		string[] array = model.SelectedValue.ToString().Split(':');
		int result = 0;
		int result2 = 0;
		if (array != null && array.Length != 0)
		{
			int.TryParse(array[0], out result);
		}
		if (array != null && array.Length != 0)
		{
			int.TryParse(array[1], out result2);
		}
		picker.MinValue = 0;
		picker.MaxValue = 23;
		picker.Value = result;
		picker.DescendantFocusability = DescendantFocusability.BlockDescendants;
		picker2.MinValue = 0;
		picker2.MaxValue = 59;
		picker2.Value = result2;
		picker2.SetDisplayedValues(array3);
		picker2.DescendantFocusability = DescendantFocusability.BlockDescendants;
		textView.Text = ":";
		textView.SetTypeface(fontFace, TypefaceStyle.Normal);
		LinearLayout linearLayout = new LinearLayout(((Android.Views.View)(object)this).Context)
		{
			Orientation = Orientation.Horizontal
		};
		linearLayout.SetGravity(GravityFlags.Center);
		linearLayout.Visibility = ViewStates.Visible;
		linearLayout.AddView(picker);
		linearLayout.AddView(textView);
		linearLayout.AddView(picker2);
		textView.Gravity = GravityFlags.CenterVertical;
		ElementController.SetValueFromRenderer(VisualElement.IsFocusedProperty, true);
		AlertDialog.Builder builder = new AlertDialog.Builder(((Android.Views.View)(object)this).Context);
		builder.SetView(linearLayout);
		builder.SetTitle(model.Title ?? "");
		builder.SetNegativeButton("キャンセル", delegate
		{
			ElementController.SetValueFromRenderer(VisualElement.IsFocusedProperty, false);
			base.Control?.ClearFocus();
			_dialog = null;
		});
		builder.SetPositiveButton("OK", delegate
		{
			ElementController.SetValueFromRenderer(Picker.SelectedIndexProperty, picker.Value);
			if (base.Element != null)
			{
				int num = ((picker.Value != 24) ? picker2.Value : 0);
				model.SelectedValue = string.Format($"{picker.Value}:{num:00}");
				ElementController.SetValueFromRenderer(VisualElement.IsFocusedProperty, false);
				base.Control?.ClearFocus();
			}
			_dialog = null;
		});
		_dialog = builder.Create();
		_dialog.DismissEvent += delegate
		{
			ElementController?.SetValueFromRenderer(VisualElement.IsFocusedProperty, false);
		};
		_dialog.Show();
		Android.Widget.Button button = _dialog.GetButton(-1);
		button.SetTypeface(fontFace, TypefaceStyle.Normal);
		button.SetTextColor(Android.Graphics.Color.ParseColor("#33b5e5"));
		button.TextSize = 16f;
		LinearLayout obj = (LinearLayout)button.Parent;
		obj.SetGravity(GravityFlags.AxisSpecified);
		obj.GetChildAt(1).Visibility = ViewStates.Gone;
	}
}
