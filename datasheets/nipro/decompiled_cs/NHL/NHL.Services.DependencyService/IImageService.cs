using System;
using Plugin.Media.Abstractions;

namespace NHL.Services.DependencyService;

public interface IImageService
{
	DateTime? GetCreateTime(string path);

	DateTime? GetCreateTime(MediaFile photo);

	string ConvertFileToBase64(string path);

	byte[] ResizeImage(string path, int width, int height, int quality);

	void ShowImageGallery();
}
