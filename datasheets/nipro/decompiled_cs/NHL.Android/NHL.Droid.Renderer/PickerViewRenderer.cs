using System;
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

public class PickerViewRenderer : PickerRenderer
{
	private Typeface fontFace;

	private AlertDialog _dialog;

	private IElementController ElementController => base.Element;

	public PickerViewRenderer(Context context)
		: base(context)
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
		PickerView model = base.Element as PickerView;
		int selectedIndex = model.SelectedIndex;
		NumberPicker picker = new NumberPicker(((Android.Views.View)(object)this).Context);
		picker.MinValue = 20;
		picker.MaxValue = 600;
		if (selectedIndex > 0)
		{
			picker.Value = selectedIndex + 20;
		}
		else
		{
			picker.Value = 20;
		}
		LinearLayout linearLayout = new LinearLayout(((Android.Views.View)(object)this).Context)
		{
			Orientation = Orientation.Horizontal
		};
		linearLayout.SetGravity(GravityFlags.Center);
		linearLayout.Visibility = ViewStates.Visible;
		linearLayout.AddView(picker);
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
			if (base.Element != null)
			{
				model.SelectedItem = picker.Value;
				model.SelectedIndex = picker.Value - 20;
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
