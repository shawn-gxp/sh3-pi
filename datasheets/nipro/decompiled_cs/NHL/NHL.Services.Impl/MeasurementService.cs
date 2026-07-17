using System;
using System.Threading.Tasks;
using Caliburn.Micro;
using NHL.Framework;
using NHL.Models.Entity;
using NHL.Services.DependencyService;
using NHL.Services.Support;
using Xamarin.Forms;

namespace NHL.Services.Impl;

public class MeasurementService(IRestClient newRestClient) : SyncServiceBase("Measurement", newRestClient), IMeasurementService, ISyncServiceBase
{
	private readonly ILoggingService _log = Xamarin.Forms.DependencyService.Get<ILoggingService>();

	public async Task<BindableCollection<Measurement>> GetAllMeasurement(bool isSync = false)
	{
		if (isSync)
		{
			_log.Info("【IG】【MeasurementService】【GetAllMeasurement】Sync start");
			await Sync();
		}
		return new BindableCollection<Measurement>(await SyncManager.GetInstance().MeasurementTable.ToEnumerableAsync());
	}

	public async Task RegisterMeasurement(Measurement measurement)
	{
		try
		{
			await SyncManager.GetInstance().MeasurementTable.InsertAsync(measurement);
		}
		catch (Exception ex)
		{
			_log.Error($"【IG】【MeasurementService】【RegisterMeasurement】InsertAsync error: {ex}");
			throw;
		}
	}

	public async Task UpdateMeasurement(Measurement measurement)
	{
		try
		{
			await SyncManager.GetInstance().MeasurementTable.UpdateAsync(measurement);
		}
		catch (Exception ex)
		{
			_log.Error($"【IG】【MeasurementService】【UpdateMeasurement】UpdateAsync error: {ex}");
			throw;
		}
	}

	public override async Task PullAsync()
	{
		await SyncManager.GetInstance().MeasurementPullAsync();
	}

	public async Task DeleteMeasurement(Measurement measurement)
	{
		try
		{
			await SyncManager.GetInstance().MeasurementTable.DeleteAsync(measurement);
		}
		catch (Exception ex)
		{
			_log.Error($"【IG】【MeasurementService】【DeleteMeasurement】DeleteAsync error: {ex}");
			throw;
		}
	}
}
