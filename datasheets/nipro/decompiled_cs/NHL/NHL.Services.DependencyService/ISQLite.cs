using SQLite.Net;

namespace NHL.Services.DependencyService;

public interface ISQLite
{
	SQLiteConnection GetConnection();
}
