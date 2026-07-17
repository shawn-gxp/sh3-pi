using System.IO;
using Microsoft.WindowsAzure.MobileServices;
using NHL.Services.DependencyService;
using SQLite.Net;
using SQLite.Net.Platform.XamarinAndroid;

namespace blood.Droid.DependencyService;

public class SQLite_Android : ISQLite
{
	public const string OFFLINE_DB_PATH = "localstore.db";

	public SQLiteConnection GetConnection()
	{
		string databasePath = Path.Combine(MobileServiceClient.DefaultDatabasePath, "localstore.db");
		return new SQLiteConnection(new SQLitePlatformAndroid(), databasePath);
	}
}
