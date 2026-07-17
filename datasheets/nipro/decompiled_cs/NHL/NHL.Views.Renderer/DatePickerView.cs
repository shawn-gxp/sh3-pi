using System;
using Xamarin.Forms;

namespace NHL.Views.Renderer;

public class DatePickerView : DatePicker
{
	public static readonly BindableProperty NullableDateProperty = BindableProperty.Create("NullableDate", typeof(DateTime?), typeof(DatePickerView), null, BindingMode.TwoWay, null, OnDateChanged);

	public string _originalFormat;

	public DateTime? NullableDate
	{
		get
		{
			return (DateTime?)GetValue(NullableDateProperty);
		}
		set
		{
			SetValue(NullableDateProperty, value);
		}
	}

	public string Locale { get; set; } = "ja-JP";

	public new int FontSize { get; set; } = 22;

	public event EventHandler ClearEvent;

	public static void OnDateChanged(BindableObject bindable, object oldValue, object newValue)
	{
		if (!(bindable is DatePickerView datePickerView))
		{
			return;
		}
		DateTime result;
		if (newValue == null)
		{
			if (datePickerView.NullableDate.HasValue)
			{
				datePickerView.NullableDate = null;
			}
			datePickerView.ClearEvent?.Invoke(datePickerView, null);
		}
		else if (DateTime.TryParse(newValue.ToString(), out result))
		{
			if (datePickerView.NullableDate != result)
			{
				datePickerView.NullableDate = result;
			}
			datePickerView.TextColor = Color.Black;
		}
		else
		{
			if (datePickerView.NullableDate.HasValue)
			{
				datePickerView.NullableDate = null;
			}
			datePickerView.ClearEvent?.Invoke(datePickerView, null);
		}
	}

	public void CleanDate()
	{
		NullableDate = null;
		UpdateDate();
	}

	public void AssignValue()
	{
		NullableDate = base.Date;
		UpdateDate();
	}

	protected override void OnBindingContextChanged()
	{
		base.OnBindingContextChanged();
		if (base.BindingContext != null)
		{
			_originalFormat = base.Format;
			UpdateDate();
		}
	}

	protected override void OnPropertyChanged(string propertyName = null)
	{
		base.OnPropertyChanged(propertyName);
		if (propertyName == DatePicker.DateProperty.PropertyName || (propertyName == VisualElement.IsFocusedProperty.PropertyName && !base.IsFocused && base.Date.ToString("d") == DateTime.Now.ToString("d")))
		{
			AssignValue();
		}
		if (propertyName == NullableDateProperty.PropertyName && NullableDate.HasValue)
		{
			base.Date = NullableDate.Value;
			if (base.Date.ToString(_originalFormat) == DateTime.Now.ToString(_originalFormat))
			{
				UpdateDate();
			}
		}
	}

	private void UpdateDate()
	{
		if (NullableDate.HasValue && _originalFormat != null)
		{
			base.Format = _originalFormat;
		}
	}
}
