using System;
using System.IO;
using Android.App;
using Android.Content;
using Android.Graphics;
using NHL.Services.DependencyService;
using Plugin.Media.Abstractions;

namespace NHL.Droid.DependencyService;

public class ImageService : IImageService
{
	public DateTime? GetCreateTime(MediaFile photo)
	{
		return GetCreateTime(photo.Path);
	}

	public DateTime? GetCreateTime(string path)
	{
		if (!File.Exists(path))
		{
			return null;
		}
		return new FileInfo(path).CreationTime;
	}

	public string ConvertFileToBase64(string path)
	{
		if (!File.Exists(path))
		{
			return null;
		}
		using FileStream fileStream = new FileStream(path, FileMode.Open, FileAccess.Read);
		byte[] array = new byte[fileStream.Length];
		fileStream.Read(array, 0, (int)fileStream.Length);
		return Convert.ToBase64String(array);
	}

	public byte[] ResizeImage(string path, int width, int height, int quality)
	{
		if (!File.Exists(path))
		{
			return null;
		}
		Bitmap shrinkedBitmap = GetShrinkedBitmap(path, width, height);
		if (shrinkedBitmap == null)
		{
			throw new ApplicationException("写真の圧縮に失敗しました。");
		}
		using MemoryStream memoryStream = new MemoryStream();
		if (!shrinkedBitmap.Compress(Bitmap.CompressFormat.Jpeg, quality, memoryStream))
		{
			throw new ApplicationException("サムネイルの生成に失敗しました。");
		}
		return memoryStream.ToArray();
	}

	public void ShowImageGallery()
	{
		Activity obj = (Activity)Android.App.Application.Context;
		Intent intent = new Intent();
		intent.SetType("image/*");
		intent.SetAction("android.intent.action.GET_CONTENT");
		obj.StartActivityForResult(Intent.CreateChooser(intent, "Select photo"), 100);
	}

	private Bitmap GetShrinkedBitmap(string filePath, int width, int height)
	{
		Bitmap result = null;
		try
		{
			BitmapFactory.Options options = new BitmapFactory.Options
			{
				InJustDecodeBounds = true
			};
			BitmapFactory.DecodeFile(filePath, options);
			if (width > 0 && height > 0)
			{
				options.InSampleSize = CalculateInSampleSize(options, width, height);
			}
			else
			{
				options.InSampleSize = CalculateInSampleSize(options, options.OutWidth, options.OutHeight);
			}
			options.InPreferredConfig = Bitmap.Config.Argb8888;
			options.InJustDecodeBounds = false;
			result = BitmapFactory.DecodeFile(filePath, options);
		}
		catch (Exception)
		{
		}
		return result;
	}

	private int CalculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
	{
		float num = options.OutHeight;
		float num2 = options.OutWidth;
		double num3 = 1.0;
		if (num > (float)reqHeight || num2 > (float)reqWidth)
		{
			int num4 = (int)(num / 2f);
			int num5 = (int)(num2 / 2f);
			while ((double)num4 / num3 > (double)reqHeight && (double)num5 / num3 > (double)reqWidth)
			{
				num3 *= 2.0;
			}
		}
		return (int)num3;
	}

	private Bitmap GetSquareBitmap(string filePath, BitmapFactory.Options option, int minValue, int targetValue)
	{
		option.InSampleSize = minValue / targetValue;
		option.InPreferredConfig = Bitmap.Config.Argb8888;
		option.InJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.DecodeFile(filePath, option);
		int num = bitmap.Height - targetValue;
		int num2 = bitmap.Width - targetValue;
		num = ((num == 0) ? num : (num / 2));
		num2 = ((num2 == 0) ? num2 : (num2 / 2));
		return Bitmap.CreateBitmap(bitmap, num2, num, targetValue, targetValue, new Matrix(), filter: true);
	}
}
