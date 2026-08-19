import type { ReactNode } from "react";
import { accessoryById } from "../lib/accessories";
import { formatQuoteAmount } from "../lib/format";
import type { AccessoryItem, CostBreakdown, VehicleDetail } from "../types";

const DEALER_NAME = "MITSUBISHI MOVEO NEW CITY - THÀNH PHỐ MỚI BÌNH DƯƠNG";
const DEALER_ADDRESS = "1C, Đường Hùng Vương, Phường Hòa Phú, TP. Thủ Dầu Một, Tỉnh Bình Dương";
const QUOTE_VALIDITY = "Hiệu lực báo giá áp dụng trong tháng";
const BANK_NOTE =
  "Quý khách đặt cọc và ký hợp đồng chúng tôi sẽ tiến hành thẩm định và làm hồ sơ ngân hàng, trường hợp ngân hàng không đồng ý cho vay, chúng tôi sẽ hoàn lại 100% tiền cọc";
const EXTRA_ACCESSORY = "Phụ kiện trang bị thêm (Nếu có)";
const BLACK_BOX = "Hộp đen (nếu có)";
const LOAN_YEARS = 5;
const LOAN_ANNUAL_RATE = 0.078;

const COLOR_SWATCH: Record<string, string> = {
  Trắng: "#f4f4f4",
  Đen: "#1a1a1a",
  Bạc: "#c5c7ca",
  Xám: "#6b7280",
  Nâu: "#6b3e26",
  Đỏ: "#c00000",
};

function feeAmount(result: CostBreakdown, code: string): number {
  const fee = result.fees.find((item) => item.code === code);
  if (!fee || !fee.includedInTotal) {
    return 0;
  }
  return Number(fee.amount) || 0;
}

function money(amount: number, blankIfZero = false): string {
  if (blankIfZero && !amount) {
    return "";
  }
  return formatQuoteAmount(amount);
}

function todayLabel(): string {
  return new Date().toLocaleDateString("vi-VN");
}

function giftPairs(gifts?: string): string[][] {
  const items = (gifts ?? "")
    .split(/[;|]/)
    .map((item) => item.trim())
    .filter(Boolean);
  const pairs: string[][] = [];
  for (let i = 0; i < items.length; i += 2) {
    pairs.push([items[i], items[i + 1] ?? ""]);
  }
  return pairs;
}

function colors(vehicle: VehicleDetail): string[] {
  return (vehicle.availableColors ?? vehicle.defaultColor ?? "")
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);
}

export function QuoteSheet({
  vehicle,
  result,
  customerName,
  customerAddress,
  color,
  selectedAccessories = [],
}: {
  vehicle: VehicleDetail;
  result: CostBreakdown;
  customerName: string;
  customerAddress: string;
  color: string;
  selectedAccessories?: AccessoryItem[];
}) {
  const listPrice = Number(result.listPrice) || 0;
  const discount = Number(result.discountAmount) || 0;
  const salePrice = Number(result.salePrice ?? vehicle.salePrice ?? listPrice - discount);
  const registrationTax = feeAmount(result, "REGISTRATION_TAX");
  const licensePlate = feeAmount(result, "LICENSE_PLATE");
  const inspection = feeAmount(result, "INSPECTION");
  const insurance = feeAmount(result, "COMPULSORY_INSURANCE");
  const roadUse = feeAmount(result, "ROAD_USE");
  const optionalBody = feeAmount(result, "OPTIONAL_BODY_INSURANCE");
  const mica = feeAmount(result, "MICA_PLATE");
  const registrationService = feeAmount(result, "REGISTRATION_SERVICE") || feeAmount(result, "REGISTRATION_FEE");
  const registrationTotal = Number(result.totalMandatoryFees) + Number(result.totalOptionalFees);
  const extrasTotal = Number(result.accessoriesTotal) || 0;
  const accessories = (result.accessories ?? []).map((item) => {
    const selected = selectedAccessories.find((extra) => extra.name === item.name);
    const catalog = accessoryById(selected?.catalogId);
    return {
      ...item,
      imageUrl: selected?.imageUrl ?? catalog?.imageUrl,
    };
  });
  const onRoadTotal = Number(result.estimatedOnRoadTotal);
  const deposit = Number(result.deposit ?? vehicle.defaultDeposit) || 0;
  const cashSecond = Math.max(onRoadTotal - deposit, 0);
  const loanAmount = Math.max(salePrice - deposit, 0);
  const months = LOAN_YEARS * 12;
  const monthlyRate = LOAN_ANNUAL_RATE / 12;
  const monthlyPrincipal = months ? loanAmount / months : 0;
  const monthlyInterest = loanAmount * monthlyRate;
  const monthlyPayment = monthlyPrincipal + monthlyInterest;
  const bankSecond = Math.max(onRoadTotal - deposit - loanAmount, 0);
  const chosenColor = color || vehicle.defaultColor || "";
  const gifts = giftPairs(vehicle.gifts);
  const feeRows = [
    { label: "Thuế trước bạ (tạm tính)", amount: registrationTax },
    { label: "Phí bấm biển số", amount: licensePlate },
    { label: "Lệ phí đăng kiểm", amount: inspection },
    { label: "Bảo hiểm TNDS + Người ngồi xe (1 năm)", amount: insurance },
    { label: "Phí sử dụng đường bộ (1 năm)", amount: roadUse },
    { label: "Bảo hiểm vật chất thân vỏ xe", amount: optionalBody },
    { label: "Biển số mica", amount: mica },
    { label: "Phí dịch vụ đăng ký xe", amount: registrationService },
  ];

  return (
    <article className="overflow-hidden rounded-sm border border-[#1f1f1f] bg-white text-[13px] text-[#1f1f1f] shadow-card print:shadow-none">
      <header className="border-b-4 border-[#e60012] bg-[#e60012] px-5 py-4 text-center text-white">
        <p className="text-lg font-black uppercase tracking-wide">{DEALER_NAME}</p>
        <p className="mt-1 text-sm font-medium">{DEALER_ADDRESS}</p>
        <p className="mt-2 text-xs uppercase tracking-[0.2em] text-white/80">Bảng báo giá chi tiết</p>
      </header>

      <div className="grid grid-cols-1 border-b border-[#1f1f1f] sm:grid-cols-2">
        <Field label="Khách hàng" value={customerName} />
        <Field label="Ngày" value={todayLabel()} />
        <Field label="Địa chỉ" value={customerAddress} />
        <Field label="TVBH / SĐT" value="" />
      </div>

      <table className="w-full border-collapse">
        <tbody>
          <tr>
            <Th>Loại xe</Th>
            <Td className="font-semibold">{vehicle.name}</Td>
            <Th>Đời xe</Th>
            <Td>{vehicle.year ?? ""}</Td>
            <Th>Màu xe</Th>
            <Td>{chosenColor}</Td>
          </tr>
          <tr>
            <Th>Giá niêm yết</Th>
            <Td className="text-right font-semibold">{money(listPrice)}</Td>
            <Th>TG giao xe</Th>
            <Td colSpan={3}>{vehicle.deliveryNote || ""}</Td>
          </tr>
          <tr>
            <Th>Giảm giá</Th>
            <Td className="text-right">{money(discount)}</Td>
            <Td colSpan={4} className="bg-[#fff2cc] font-medium italic">
              {QUOTE_VALIDITY}
            </Td>
          </tr>
          <tr>
            <Th className="bg-[#fff2cc]">Giá Bán</Th>
            <Td className="bg-[#fff2cc] text-right text-base font-black">{money(salePrice)}</Td>
            <Td colSpan={4} className="font-medium">
              ĐVT: VNĐ
            </Td>
          </tr>
        </tbody>
      </table>

      <div className="grid grid-cols-1 lg:grid-cols-2">
        <section>
          <SectionTitle>Tạm tính chi phí</SectionTitle>
          <table className="w-full border-collapse">
            <tbody>
              {feeRows.map((row) => (
                <tr key={row.label}>
                  <Td className="w-[62%]">{row.label}</Td>
                  <Td className="text-right font-medium">{money(row.amount, true)}</Td>
                </tr>
              ))}
              <tr>
                <Th className="bg-[#f4b183]">Tổng Chi Phí Đăng ký xe</Th>
                <Td className="bg-[#f4b183] text-right font-black">{money(registrationTotal)}</Td>
              </tr>
            </tbody>
          </table>
        </section>

        <section className="border-t border-[#1f1f1f] lg:border-l lg:border-t-0">
          <SectionTitle>Quà Tặng</SectionTitle>
          <table className="w-full border-collapse">
            <tbody>
              {gifts.length === 0 && (
                <tr>
                  <Td colSpan={2} className="text-[#6b7280]">
                    —
                  </Td>
                </tr>
              )}
              {gifts.map(([left, right]) => (
                <tr key={`${left}-${right}`}>
                  <Td>{left}</Td>
                  <Td>{right}</Td>
                </tr>
              ))}
              <tr>
                <Th colSpan={2} className="bg-[#fff2cc] text-center">
                  CHI PHÍ PHÁT SINH THÊM
                </Th>
              </tr>
              {accessories.length === 0 && (
                <tr>
                  <Td colSpan={2}>{EXTRA_ACCESSORY}</Td>
                </tr>
              )}
              {accessories.map((item) => (
                <tr key={`${item.name}-${item.amount}`}>
                  <Td>
                    <div className="flex items-center gap-2">
                      {item.imageUrl && (
                        <img src={item.imageUrl} alt="" className="h-9 w-12 rounded object-cover" />
                      )}
                      <span>{item.name}</span>
                    </div>
                  </Td>
                  <Td className="text-right font-medium">{money(Number(item.amount))}</Td>
                </tr>
              ))}
              <tr>
                <Td>{BLACK_BOX}</Td>
                <Td className="font-semibold text-[#e60012]">Tặng</Td>
              </tr>
            </tbody>
          </table>
        </section>
      </div>

      <table className="w-full border-collapse">
        <tbody>
          <tr>
            <Th className="bg-[#e60012] w-[28%] text-white">TỔNG LĂN BÁNH</Th>
            <Td className="bg-[#e60012] text-right text-lg font-black text-white">{money(onRoadTotal)}</Td>
            <Th className="bg-[#fff2cc]">TỔNG CP PHÁT SINH</Th>
            <Td className="bg-[#fff2cc] text-right font-semibold">{money(extrasTotal)}</Td>
          </tr>
        </tbody>
      </table>

      <div className="grid grid-cols-1 lg:grid-cols-2">
        <section>
          <SectionTitle>PHƯƠNG ÁN: MUA TIỀN MẶT</SectionTitle>
          <table className="w-full border-collapse">
            <tbody>
              <tr>
                <Td>Tiền cọc</Td>
                <Td className="text-right font-semibold">{money(deposit)}</Td>
              </tr>
              <tr>
                <Td>Chi Phí Phát sinh thêm (Nếu có)</Td>
                <Td className="text-right">{money(extrasTotal)}</Td>
              </tr>
              <tr>
                <Th className="bg-[#fff2cc]">THANH TOÁN LẦN 2</Th>
                <Td className="bg-[#fff2cc] text-right font-black">{money(cashSecond)}</Td>
              </tr>
            </tbody>
          </table>
        </section>
        <section className="border-t border-[#1f1f1f] lg:border-l lg:border-t-0">
          <SectionTitle>PHƯƠNG ÁN: MUA VAY NGÂN HÀNG</SectionTitle>
          <table className="w-full border-collapse">
            <tbody>
              <tr>
                <Td>Tiền cọc</Td>
                <Td className="text-right font-semibold">{money(deposit)}</Td>
              </tr>
              <tr>
                <Td>Số tiền vay</Td>
                <Td className="text-right">{money(loanAmount)}</Td>
              </tr>
              <tr>
                <Th className="bg-[#fff2cc]">THANH TOÁN LẦN 2</Th>
                <Td className="bg-[#fff2cc] text-right font-black">{money(bankSecond)}</Td>
              </tr>
            </tbody>
          </table>
        </section>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2">
        <section>
          <SectionTitle>CÁC MÀU XE</SectionTitle>
          <div className="flex flex-wrap gap-3 border-b border-[#1f1f1f] px-4 py-4">
            {colors(vehicle).map((name) => (
              <div key={name} className="flex items-center gap-2">
                <span
                  className="h-7 w-7 rounded-full border border-[#1f1f1f]/30"
                  style={{ background: COLOR_SWATCH[name] ?? "#d1d5db" }}
                />
                <span className={name === chosenColor ? "font-bold text-[#e60012]" : ""}>{name}</span>
              </div>
            ))}
          </div>
        </section>
        <section className="border-t border-[#1f1f1f] lg:border-l lg:border-t-0">
          <SectionTitle>PHƯƠNG ÁN TRẢ HÀNG THÁNG</SectionTitle>
          <table className="w-full border-collapse">
            <tbody>
              <tr>
                <Td>Thời gian vay</Td>
                <Td className="text-right">
                  {LOAN_YEARS} Năm / {months} tháng
                </Td>
              </tr>
              <tr>
                <Td>Tiền gốc tháng</Td>
                <Td className="text-right">{money(monthlyPrincipal)}</Td>
              </tr>
              <tr>
                <Td>Lãi suất</Td>
                <Td className="text-right">{(LOAN_ANNUAL_RATE * 100).toFixed(1)}% / năm</Td>
              </tr>
              <tr>
                <Th className="bg-[#fff2cc]">Thanh toán tháng</Th>
                <Td className="bg-[#fff2cc] text-right font-black">{money(monthlyPayment)}</Td>
              </tr>
            </tbody>
          </table>
          <p className="border-b border-[#1f1f1f] px-4 py-3 text-[11px] leading-relaxed text-[#4b5563]">{BANK_NOTE}</p>
        </section>
      </div>

      <p className="border-b border-[#1f1f1f] px-4 py-3 text-sm">
        * Chính sách bảo hành: {vehicle.warrantyNote || "3 năm/100.000km"} tùy theo điều kiện nào đến trước
      </p>

      <div className="grid grid-cols-2 text-center">
        <div className="border-r border-[#1f1f1f] px-4 py-5">
          <p className="font-black uppercase tracking-wide">XÁC NHẬN TVBH</p>
          <p className="mt-16 text-xs text-[#6b7280]">Ký và ghi rõ họ tên</p>
        </div>
        <div className="px-4 py-5">
          <p className="font-black uppercase tracking-wide">XÁC NHẬN KHÁCH HÀNG</p>
          <p className="mt-16 text-xs text-[#6b7280]">Ký và ghi rõ họ tên</p>
        </div>
      </div>
    </article>
  );
}

function SectionTitle({ children }: { children: string }) {
  return (
    <div className="bg-[#e60012] px-4 py-2 text-center text-sm font-black uppercase tracking-wide text-white">
      {children}
    </div>
  );
}

function Field({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex gap-2 border-b border-[#1f1f1f]/40 px-4 py-2">
      <span className="shrink-0 font-semibold">{label}:</span>
      <span className="min-h-5 font-medium">{value}</span>
    </div>
  );
}

function Th({ children, className = "", colSpan }: { children: string; className?: string; colSpan?: number }) {
  return (
    <th
      colSpan={colSpan}
      className={`border border-[#1f1f1f] bg-[#f3f3f3] px-3 py-2 text-left font-semibold ${className}`}
    >
      {children}
    </th>
  );
}

function Td({
  children,
  className = "",
  colSpan,
}: {
  children?: ReactNode;
  className?: string;
  colSpan?: number;
}) {
  return (
    <td colSpan={colSpan} className={`border border-[#1f1f1f] px-3 py-2 ${className}`}>
      {children}
    </td>
  );
}
