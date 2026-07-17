using System;
using System.ComponentModel;
using Android.App;
using Android.Views;
using Android.Widget;
using NHL.Views.Renderer;
using Xamarin.Forms;
using Xamarin.Forms.Platform.Android;

namespace NHL.Droid.Renderer;

public class DatePickerViewRenderer : ViewRenderer<DatePickerView, EditText>
{
	private DatePickerDialog _dialog;

	protected override void OnElementChanged(ElementChangedEventArgs<DatePickerView> e)
	{
		base.OnElementChanged(e);
		if (e.OldElement == null)
		{
			EditText nativeControl = new EditText(Forms.Context)
			{
				TextSize = base.Element.FontSize
			};
			SetNativeControl(nativeControl);
		}
		base.Control.Click += OnPickerClick;
		base.Control.KeyListener = null;
		base.Control.FocusChange += OnPickerFocusChange;
		base.Control.Enabled = base.Element.IsEnabled;
		base.Element.ClearEvent += OnClear;
	}

	protected override void OnElementPropertyChanged(object sender, PropertyChangedEventArgs e)
	{
		base.OnElementPropertyChanged(sender, e);
		if (e.PropertyName == Xamarin.Forms.DatePicker.DateProperty.PropertyName || e.PropertyName == Xamarin.Forms.DatePicker.FormatProperty.PropertyName)
		{
			SetDate(base.Element.Date);
		}
	}

	protected override void Dispose(bool disposing)
	{
		if (base.Control != null)
		{
			base.Control.Click -= OnPickerClick;
			base.Control.FocusChange -= OnPickerFocusChange;
			if (_dialog != null)
			{
				_dialog.Hide();
				_dialog.Dispose();
				_dialog = null;
			}
		}
		if (base.Element != null)
		{
			base.Element.ClearEvent -= OnClear;
		}
		base.Dispose(disposing);
	}

	private void OnPickerFocusChange(object sender, Android.Views.View.FocusChangeEventArgs e)
	{
		if (e.HasFocus)
		{
			ShowDatePicker();
		}
	}

	private void OnPickerClick(object sender, EventArgs e)
	{
		ShowDatePicker();
	}

	private void SetDate(DateTime date)
	{
		base.Control.Text = date.ToString(base.Element.Format);
		base.Element.Date = date;
	}

	private void ShowDatePicker()
	{
		CreateDatePickerDialog(base.Element.Date.Year, base.Element.Date.Month - 1, base.Element.Date.Day);
		_dialog.Show();
	}

	private void CreateDatePickerDialog(int year, int month, int day)
	{
		DatePickerView view = base.Element;
		_dialog = new DatePickerDialog(((Android.Views.View)(object)this).Context, delegate(object o, DatePickerDialog.DateSetEventArgs e)
		{
			view.Date = e.Date;
			((IElementController)view).SetValueFromRenderer(VisualElement.IsFocusedProperty, (object)false);
			base.Control.ClearFocus();
			_dialog = null;
		}, year, month, day);
		_dialog.SetButton("OK", delegate
		{
			SetDate(_dialog.DatePicker.DateTime);
			base.Element.Format = base.Element._originalFormat;
			base.Element.AssignValue();
		});
		_dialog.SetButton2("キャンセル", delegate
		{
			base.Element.CleanDate();
			base.Control.Text = string.Empty;
		});
	}

	private void OnClear(object sender, EventArgs e)
	{
		base.Control.Text = string.Empty;
	}
}
