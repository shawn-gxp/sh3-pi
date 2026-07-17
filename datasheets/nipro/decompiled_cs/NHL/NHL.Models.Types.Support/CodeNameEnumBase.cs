namespace NHL.Models.Types.Support;

public abstract class CodeNameEnumBase
{
	public string Code { get; private set; }

	public string Name { get; private set; }

	protected CodeNameEnumBase(string newCode, string newName)
	{
		Code = newCode;
		Name = newName;
	}

	public override string ToString()
	{
		return string.Format("{0} {1}", new object[2] { Code, Name });
	}
}
